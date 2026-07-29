/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.game.plugin.vpl

import android.content.Context
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.plugin.vpl.PluginCandidateRepository.PluginCandidate
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.logging.Logger
import com.vpl.verifiedpluginload.api.VerifiedPluginLoad
import com.vpl.verifiedpluginload.api.VerifiedPluginLoadRegistry
import com.vpl.verifiedpluginload.model.AuthorType
import com.vpl.verifiedpluginload.model.PluginLoadAuthorization
import com.vpl.verifiedpluginload.model.PluginTrustStatus
import com.vpl.verifiedpluginload.model.PluginVerificationResult
import com.vpl.verifiedpluginload.model.TrustActionStatus
import com.vpl.verifiedpluginload.model.TrustedAuthorInfo
import kotlinx.coroutines.CancellationException

/** Presents one source-bound confirmation at a time before any native plugin path is used. */
object PluginTrustGate {
    private const val TAG = "VerifiedPluginLoad"
    private const val UNKNOWN_PLUGIN_COOLDOWN_SECONDS = 6

    suspend fun verifyForLaunch(
        context: Context,
        onDialogShow: (AndroidStringText) -> Unit = {}
    ): List<PluginLoadAuthorization> {
        PluginTrustListSync.awaitStartupRefresh()

        val candidates = PluginCandidateRepository.forLaunch()
        if (candidates.isEmpty()) return emptyList()

        val vpl = VerifiedPluginLoadRegistry.get(context)
        return verifyNext(vpl, candidates, 0, mutableListOf(), onDialogShow)
    }

    fun isVerificationRequired(): Boolean = PluginCandidateRepository.forLaunch().isNotEmpty()

    /** MainActivity invokes this for handled configuration changes such as screen rotation. */
    fun resetUnknownPluginCooldown() {
        PluginTrustDialogState.restartCooldown()
    }

    private suspend fun verifyNext(
        vpl: VerifiedPluginLoad,
        candidates: List<PluginCandidate>,
        index: Int,
        authorizations: MutableList<PluginLoadAuthorization>,
        onDialogShow: (AndroidStringText) -> Unit
    ): List<PluginLoadAuthorization> {
        if (index >= candidates.size) return authorizations.toList()

        val candidate = candidates[index]
        val result = vpl.inspectInstalledPackage(candidate.packageName)
        logDecision(candidate, result)

        val allowUntrusted = AllSettings.allowUntrustedPlugins.getValue()

        return when (result.status) {
            PluginTrustStatus.TRUSTED -> {
                if (!PluginNativeLoadGuard.isExplicitKeyTrustAllowed(result.trustSource, allowUntrusted)) {
                    closeWithFailure(
                        title = androidText(R.string.plugin_trust_title_key_trust_disabled),
                        summary = androidText(R.string.plugin_trust_summary_key_trust_disabled),
                        message = androidText(R.string.plugin_trust_key_trust_disabled_body),
                        sections = buildDetailSections(candidate, result),
                        onDialogShow = onDialogShow
                    )
                    throw CancellationException("Key trust disabled")
                }
                authorizations.add(requireAuthorization(result))
                verifyNext(vpl, candidates, index + 1, authorizations, onDialogShow)
            }

            PluginTrustStatus.PENDING_TRUST ->
                requestAuthorTrust(vpl, candidate, result, candidates, index, authorizations, onDialogShow)

            PluginTrustStatus.UNTRUSTED -> {
                if (allowUntrusted) {
                    requestKeyTrust(vpl, candidate, result, candidates, index, authorizations, onDialogShow)
                } else {
                    closeWithFailure(
                        title = androidText(R.string.plugin_trust_title_untrusted),
                        summary = androidText(R.string.plugin_trust_summary_untrusted),
                        message = androidText(R.string.plugin_trust_untrusted_body),
                        sections = buildDetailSections(candidate, result),
                        onDialogShow = onDialogShow
                    )
                    throw CancellationException("Untrusted plugin")
                }
            }

            PluginTrustStatus.BANNED -> {
                val reasonFact = result.keyDescription
                    ?.let { TrustFact.sanitized(R.string.plugin_trust_label_reason, it, 160) }
                    ?: TrustFact.resource(
                        R.string.plugin_trust_label_reason,
                        R.string.plugin_trust_banned_reason_default
                    )
                closeWithFailure(
                    title = androidText(R.string.plugin_trust_title_banned),
                    summary = androidText(R.string.plugin_trust_summary_banned),
                    message = androidText(
                        R.string.plugin_trust_banned_body,
                        bannedWarningText(result.author)
                    ),
                    sections = buildDetailSections(
                        candidate, result,
                        reasonFact
                    ),
                    onDialogShow = onDialogShow
                )
                throw CancellationException("Banned plugin")
            }

            PluginTrustStatus.VERIFICATION_FAILED -> {
                closeWithFailure(
                    title = androidText(R.string.plugin_trust_title_failed),
                    summary = androidText(R.string.plugin_trust_summary_failed),
                    message = androidText(R.string.plugin_trust_failed_body),
                    sections = buildDetailSections(
                        candidate, result,
                        TrustFact.literal(R.string.plugin_trust_label_check_code, result.diagnostic.name)
                    ),
                    onDialogShow = onDialogShow
                )
                throw CancellationException("Verification failed")
            }
        }
    }

    private suspend fun requestAuthorTrust(
        vpl: VerifiedPluginLoad,
        candidate: PluginCandidate,
        result: PluginVerificationResult,
        candidates: List<PluginCandidate>,
        index: Int,
        authorizations: MutableList<PluginLoadAuthorization>,
        onDialogShow: (AndroidStringText) -> Unit
    ): List<PluginLoadAuthorization> {
        val author = result.author
        if (author == null || author.confidence == 0) {
            closeWithFailure(
                title = androidText(R.string.plugin_trust_title_registered),
                summary = androidText(R.string.plugin_trust_summary_registered),
                message = confidenceText(author),
                sections = buildDetailSections(candidate, result),
                onDialogShow = onDialogShow
            )
            throw CancellationException("Author not trustable")
        }

        val severity = if (author.confidence == 1)
            PluginTrustDialogState.Severity.WARNING
        else
            PluginTrustDialogState.Severity.INFO

        val title = androidText(R.string.plugin_trust_title_registered)
        onDialogShow(title)
        val action = PluginTrustDialogState.showAuthorTrust(
            title = title,
            summary = androidText(R.string.plugin_trust_summary_registered),
            message = confidenceText(author),
            sections = buildDetailSections(candidate, result),
            severity = severity
        )

        if (action == PluginTrustDialogState.DialogAction.CANCEL) {
            throw CancellationException("User cancelled author trust")
        }

        return trustAuthorThenContinue(vpl, candidate, result, candidates, index, authorizations, onDialogShow)
    }

    private suspend fun trustAuthorThenContinue(
        vpl: VerifiedPluginLoad,
        candidate: PluginCandidate,
        result: PluginVerificationResult,
        candidates: List<PluginCandidate>,
        index: Int,
        authorizations: MutableList<PluginLoadAuthorization>,
        onDialogShow: (AndroidStringText) -> Unit
    ): List<PluginLoadAuthorization> {
        val author = result.author ?: throw SecurityException("No author info")
        val action = vpl.trustAuthor(author.uuid)
        if (action.status != TrustActionStatus.SUCCESS) {
            throw SecurityException("Could not store author trust: ${action.status}")
        }
        val refreshed = vpl.inspectInstalledPackage(candidate.packageName)
        if (refreshed.status != PluginTrustStatus.TRUSTED) {
            throw SecurityException("Plugin is not trusted after author confirmation: ${refreshed.status}")
        }
        authorizations.add(requireAuthorization(refreshed))
        return verifyNext(vpl, candidates, index + 1, authorizations, onDialogShow)
    }

    private suspend fun requestKeyTrust(
        vpl: VerifiedPluginLoad,
        candidate: PluginCandidate,
        result: PluginVerificationResult,
        candidates: List<PluginCandidate>,
        index: Int,
        authorizations: MutableList<PluginLoadAuthorization>,
        onDialogShow: (AndroidStringText) -> Unit
    ): List<PluginLoadAuthorization> {
        if (result.currentSignatures.isEmpty()) {
            closeWithFailure(
                title = androidText(R.string.plugin_trust_title_failed),
                summary = androidText(R.string.plugin_trust_summary_failed),
                message = androidText(R.string.plugin_trust_failed_body),
                sections = buildDetailSections(
                    candidate, result,
                    TrustFact.literal(R.string.plugin_trust_label_check_code, "APK_UNSIGNED")
                ),
                onDialogShow = onDialogShow
            )
            throw CancellationException("Unsigned APK")
        }

        val title = androidText(R.string.plugin_trust_title_untrusted)
        onDialogShow(title)
        val action = PluginTrustDialogState.showKeyTrust(
            title = title,
            summary = androidText(R.string.plugin_trust_summary_unknown),
            message = androidText(R.string.plugin_trust_unknown_body),
            sections = buildDetailSections(candidate, result),
            cooldownSeconds = UNKNOWN_PLUGIN_COOLDOWN_SECONDS
        )

        if (action == PluginTrustDialogState.DialogAction.CANCEL) {
            throw CancellationException("User cancelled key trust")
        }

        return trustKeyThenContinue(vpl, candidate, result, candidates, index, authorizations, onDialogShow)
    }

    private suspend fun trustKeyThenContinue(
        vpl: VerifiedPluginLoad,
        candidate: PluginCandidate,
        result: PluginVerificationResult,
        candidates: List<PluginCandidate>,
        index: Int,
        authorizations: MutableList<PluginLoadAuthorization>,
        onDialogShow: (AndroidStringText) -> Unit
    ): List<PluginLoadAuthorization> {
        val keyHash = result.currentSignatures.first().value
        val action = vpl.trustKeyHash(keyHash)
        if (action.status != TrustActionStatus.SUCCESS) {
            throw SecurityException("Could not store key trust: ${action.status}")
        }
        val refreshed = vpl.inspectInstalledPackage(candidate.packageName)
        if (refreshed.status != PluginTrustStatus.TRUSTED) {
            throw SecurityException("Plugin is not trusted after key confirmation: ${refreshed.status}")
        }
        authorizations.add(requireAuthorization(refreshed))
        return verifyNext(vpl, candidates, index + 1, authorizations, onDialogShow)
    }

    private suspend fun closeWithFailure(
        title: AndroidStringText,
        summary: AndroidStringText?,
        message: AndroidStringText?,
        sections: List<TrustSection>,
        onDialogShow: (AndroidStringText) -> Unit
    ) {
        onDialogShow(title)
        PluginTrustDialogState.showError(title, summary, message, sections)
    }

    private fun requireAuthorization(result: PluginVerificationResult): PluginLoadAuthorization {
        return result.toLoadAuthorization()
            ?: throw SecurityException("Trusted result does not contain a load authorization")
    }

    private fun logDecision(candidate: PluginCandidate, result: PluginVerificationResult) {
        val sha256 = result.matchedSignature?.sha256 ?: "unknown"
        Logger.info(
            TAG,
            "Plugin verification: type=${candidate.type.name}, " +
                    "package=${candidate.packageName}, " +
                    "version=${result.packageInfo.versionName}, " +
                    "sha256=$sha256, " +
                    "status=${result.status}, " +
                    "trustListVersion=${result.trustListVersion}"
        )
    }

    /**
     * Builds the labelled sections. Plugin-supplied text can only ever become a value here, never a
     * heading, and every value is bounded, so the dialog's shape is fixed by the launcher no matter
     * what a plugin declares.  The registered-publisher section is emitted only when the signed list
     * actually names a publisher, so its presence is itself a claim a plugin cannot fabricate.
     */
    internal fun buildDetailSections(
        candidate: PluginCandidate,
        result: PluginVerificationResult,
        vararg extraDiagnosisFacts: TrustFact
    ): List<TrustSection> {
        val sections = mutableListOf<TrustSection>()

        val label = result.packageInfo.applicationLabel?.takeIf { it.isNotBlank() } ?: candidate.packageName
        sections += TrustSection(
            R.string.plugin_trust_section_plugin,
            listOf(
                TrustFact.sanitized(R.string.plugin_trust_label_name, label, 64),
                TrustFact.sanitized(R.string.plugin_trust_label_version, result.packageInfo.versionName, 32),
                TrustFact(R.string.plugin_trust_label_type, candidate.type.localeText)
            )
        )

        val author = result.author
        if (author != null) {
            sections += TrustSection(
                R.string.plugin_trust_section_publisher,
                listOf(
                    TrustFact.sanitized(R.string.plugin_trust_label_name, author.name, 128),
                    TrustFact.resource(
                        R.string.plugin_trust_label_type,
                        if (author.type == AuthorType.ORG) R.string.plugin_trust_author_org
                        else R.string.plugin_trust_author_person
                    ),
                    TrustFact.sanitized(R.string.plugin_trust_label_description, author.description, 128),
                    TrustFact.sanitized(R.string.plugin_trust_label_website, author.web, 128)
                )
            )
        }

        val packageName = result.packageInfo.packageName ?: candidate.packageName
        val sha256 = result.currentSignatures.firstOrNull()?.sha256
        sections += TrustSection(
            R.string.plugin_trust_section_signature,
            listOf(
                TrustFact.sanitized(R.string.plugin_trust_label_package, packageName, 128),
                TrustFact.literal(R.string.plugin_trust_label_fingerprint, formatFingerprint(sha256), monospace = true)
            )
        )

        if (extraDiagnosisFacts.isNotEmpty()) {
            sections += TrustSection(R.string.plugin_trust_section_diagnosis, extraDiagnosisFacts.toList())
        }

        return sections
    }

    private fun bannedWarningText(author: TrustedAuthorInfo?): AndroidStringText =
        if (author != null && author.confidence == 0)
            androidText(R.string.plugin_trust_banned_confidence_0)
        else
            androidText(R.string.plugin_trust_banned_confidence_known)

    private fun confidenceText(author: TrustedAuthorInfo?): AndroidStringText {
        if (author == null) return androidText(R.string.plugin_trust_confidence_0)
        return when (author.confidence) {
            2 -> androidText(R.string.plugin_trust_confidence_2)
            1 -> androidText(R.string.plugin_trust_confidence_1)
            else -> androidText(R.string.plugin_trust_confidence_0)
        }
    }
}
