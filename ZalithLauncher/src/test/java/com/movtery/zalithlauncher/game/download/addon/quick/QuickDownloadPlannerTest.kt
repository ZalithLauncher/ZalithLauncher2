/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.game.download.addon.quick

import com.movtery.zalithlauncher.game.download.addon.platform.Platform
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformDependencyType
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformDisplayLabel
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformProject
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformReleaseType
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformVersion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.File
import java.io.IOException
import java.time.Instant

class QuickDownloadPlannerTest {
    @Test
    fun selectsLatestCompatibleFileAndDeduplicatesSharedDirectory() = runBlocking<Unit> {
        val old = version("root-old", "root", day = "2025-01-01")
        val latest = version("root-new", "root", day = "2025-01-03")
        val otherGame = version("root-other", "root", day = "2025-01-04", games = arrayOf("1.21"))
        val planner = planner(versions = mapOf("MODRINTH/root" to listOf(old, otherGame, latest)))
        val sharedDir = File("build/quick-planner/shared").canonicalFile

        val plan = planner.plan(
            request(),
            listOf(target("one", sharedDir), target("two", sharedDir))
        )

        assertEquals(listOf("root-new"), plan.map { it.file.versionId })
        assertEquals(1, plan.single().targets.size)
    }

    @Test
    fun differentReleasesOfOneProjectCannotShareAnInstallDirectory() {
        val planner = planner(
            versions = mapOf(
                "MODRINTH/root" to listOf(
                    version("root-1.20", "root", fileName = "root-1.20.jar"),
                    version("root-1.21", "root", games = arrayOf("1.21"), fileName = "root-1.21.jar")
                )
            )
        )
        val sharedDir = File("build/quick-planner/shared-conflict").canonicalFile

        assertThrows(QuickPlanningException::class.java) {
            runBlocking {
                planner.plan(
                    request(),
                    listOf(
                        target("one", sharedDir),
                        target("two", sharedDir).copy(minecraftVersion = "1.21")
                    )
                )
            }
        }
    }

    @Test
    fun recursivelyPlansOnlyRequiredDependencies() = runBlocking<Unit> {
        val root = version(
            "root-v1", "root", dependencies = listOf(
                dependency("dep", type = PlatformDependencyType.REQUIRED),
                dependency("optional", type = PlatformDependencyType.OPTIONAL)
            )
        )
        val dep = version(
            "dep-v1", "dep", dependencies = listOf(
                dependency("transitive", type = PlatformDependencyType.REQUIRED)
            )
        )
        val transitive = version("transitive-v1", "transitive")
        val optional = version("optional-v1", "optional")
        val planner = planner(
            versions = mapOf(
                "MODRINTH/root" to listOf(root),
                "MODRINTH/dep" to listOf(dep),
                "MODRINTH/transitive" to listOf(transitive),
                "MODRINTH/optional" to listOf(optional)
            )
        )

        val plan = planner.plan(request(), listOf(target()))

        assertEquals(setOf("root", "dep", "transitive"), plan.map { it.key.projectId }.toSet())
        assertFalse(plan.any { it.key.projectId == "optional" })
    }

    @Test
    fun installedDependencyIsSkippedButExplicitRootIsNot() = runBlocking<Unit> {
        val root = version(
            "root-v1", "root", dependencies = listOf(
                dependency("dep", type = PlatformDependencyType.REQUIRED)
            )
        )
        val dep = version(
            "dep-v1", "dep", dependencies = listOf(
                dependency("transitive", type = PlatformDependencyType.REQUIRED)
            )
        )
        val planner = planner(
            versions = mapOf(
                "MODRINTH/root" to listOf(root),
                "MODRINTH/dep" to listOf(dep),
                "MODRINTH/transitive" to listOf(version("transitive-v1", "transitive"))
            ),
            installed = setOf("dep")
        )

        val plan = planner.plan(request(), listOf(target()))

        assertEquals(listOf("root"), plan.map { it.key.projectId })
    }

    @Test
    fun exactDependencyVersionDoesNotDriftToLatest() = runBlocking<Unit> {
        val pinned = version("dep-old", "dep", day = "2025-01-01")
        val latest = version("dep-new", "dep", day = "2025-01-03")
        val root = version(
            "root-v1", "root", dependencies = listOf(
                dependency("dep", versionId = "dep-old", type = PlatformDependencyType.REQUIRED)
            )
        )
        val planner = planner(
            versions = mapOf(
                "MODRINTH/root" to listOf(root),
                "MODRINTH/dep" to listOf(latest, pinned)
            ),
            exact = mapOf("dep-old" to pinned)
        )

        val plan = planner.plan(request(), listOf(target()))

        assertEquals(setOf("root-v1", "dep-old"), plan.map { it.file.versionId }.toSet())
    }

    @Test
    fun installedProjectOnlySatisfiesExactDependencyWhenVersionMatches() = runBlocking<Unit> {
        val pinned = version("dep-v2", "dep")
        val root = version(
            "root-v1", "root", dependencies = listOf(dependency("dep", versionId = "dep-v2"))
        )
        val versions = mapOf("MODRINTH/root" to listOf(root))
        val exact = mapOf("dep-v2" to pinned)

        val olderInstalled = planner(
            versions = versions,
            exact = exact,
            installed = setOf("dep"),
            installedVersions = setOf("dep-v1")
        ).plan(request(), listOf(target()))
        assertEquals(setOf("root-v1", "dep-v2"), olderInstalled.map { it.file.versionId }.toSet())

        val exactInstalled = planner(
            versions = versions,
            exact = exact,
            installed = setOf("dep"),
            installedVersions = setOf("dep-v2")
        ).plan(request(), listOf(target()))
        assertEquals(listOf("root-v1"), exactInstalled.map { it.file.versionId })
    }

    @Test
    fun dependencyCycleTerminates() = runBlocking<Unit> {
        val root = version("root-v1", "root", dependencies = listOf(dependency("a")))
        val a = version("a-v1", "a", dependencies = listOf(dependency("b")))
        val b = version("b-v1", "b", dependencies = listOf(dependency("a")))
        val planner = planner(
            versions = mapOf(
                "MODRINTH/root" to listOf(root),
                "MODRINTH/a" to listOf(a),
                "MODRINTH/b" to listOf(b)
            )
        )

        val plan = planner.plan(request(), listOf(target()))

        assertEquals(setOf("root", "a", "b"), plan.map { it.key.projectId }.toSet())
    }

    @Test
    fun modQuickDownloadRequiresRecognizedMatchingLocalLoader() {
        val planner = planner(
            versions = mapOf(
                "MODRINTH/root" to listOf(version("root-v1", "root", loaders = arrayOf("Forge")))
            )
        )

        val compatible = runBlocking {
            planner.plan(request(), listOf(target().copy(loaders = listOf("Forge"))))
        }
        assertEquals(listOf("root-v1"), compatible.map { it.file.versionId })

        val unsupportedLoaders = listOf<List<String>?>(
            emptyList(),
            listOf("OptiFine"),
            listOf("Fabric"),
            null
        )
        unsupportedLoaders.forEach { loaders ->
            val issues = mutableListOf<QuickPlanningIssue>()
            val plan = runBlocking {
                planner.plan(
                    request = request(),
                    targets = listOf(target().copy(loaders = loaders)),
                    onIssue = issues::add
                )
            }
            assertEquals(0, plan.size)
            assertEquals(listOf("root"), issues.map { it.projectId })
        }
    }

    @Test
    fun modQuickDownloadWithoutDeclaredLoaderIsSkippedAndReported() {
        val planner = planner(
            versions = mapOf(
                "MODRINTH/root" to listOf(version("root-v1", "root", loaders = emptyArray()))
            )
        )
        val issues = mutableListOf<QuickPlanningIssue>()

        val plan = runBlocking {
            planner.plan(request(), listOf(target()), onIssue = issues::add)
        }

        assertEquals(0, plan.size)
        assertEquals(listOf("root"), issues.map { it.projectId })
    }

    @Test
    fun exactModDependencyMismatchIsSkippedAndReported() {
        val root = version(
            "root-v1", "root", dependencies = listOf(dependency("dep", versionId = "dep-v1"))
        )
        val pinned = version("dep-v1", "dep", loaders = arrayOf("Forge"))
        val planner = planner(
            versions = mapOf("MODRINTH/root" to listOf(root)),
            exact = mapOf("dep-v1" to pinned)
        )
        val issues = mutableListOf<QuickPlanningIssue>()

        val plan = runBlocking {
            planner.plan(request(), listOf(target()), onIssue = issues::add)
        }

        assertEquals(listOf("root-v1"), plan.map { it.file.versionId })
        assertEquals(listOf("dep"), issues.map { it.projectId })
    }

    @Test
    fun differentResolvedDependencyVersionsConflictRegardlessOfClaimOrder() {
        val pinned = version("dep-old", "dep", day = "2025-01-01")
        val latest = version("dep-new", "dep", day = "2025-01-03")
        val constraints = listOf(
            listOf(dependency("dep", versionId = "dep-old"), dependency("dep")),
            listOf(dependency("dep"), dependency("dep", versionId = "dep-old"))
        )

        constraints.forEach { dependencies ->
            val planner = planner(
                versions = mapOf(
                    "MODRINTH/root" to listOf(version("root-v1", "root", dependencies = dependencies)),
                    "MODRINTH/dep" to listOf(latest, pinned)
                ),
                exact = mapOf("dep-old" to pinned)
            )

            assertThrows(QuickPlanningException::class.java) {
                runBlocking { planner.plan(request(), listOf(target())) }
            }
        }
    }

    @Test
    fun matchingPinnedAndProjectDependencyConstraintsAreAccepted() {
        val latest = version("dep-new", "dep", day = "2025-01-03")
        val constraints = listOf(
            listOf(dependency("dep", versionId = "dep-new"), dependency("dep")),
            listOf(dependency("dep"), dependency("dep", versionId = "dep-new"))
        )

        constraints.forEach { dependencies ->
            val planner = planner(
                versions = mapOf(
                    "MODRINTH/root" to listOf(version("root-v1", "root", dependencies = dependencies)),
                    "MODRINTH/dep" to listOf(latest)
                ),
                exact = mapOf("dep-new" to latest)
            )

            val plan = runBlocking { planner.plan(request(), listOf(target())) }
            assertEquals(setOf("root-v1", "dep-new"), plan.map { it.file.versionId }.toSet())
        }
    }

    @Test
    fun saveArchiveBasenameCannotEscapeInstallDirectory() {
        val planner = planner(
            versions = mapOf(
                "MODRINTH/root" to listOf(version("root-v1", "root", fileName = "...zip"))
            )
        )

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { planner.plan(request(PlatformClasses.SAVES), listOf(target())) }
        }
    }

    @Test
    fun incompatibleTargetIsSkippedWithoutBlockingCompatibleTargets() {
        val planner = planner(
            versions = mapOf(
                "MODRINTH/root" to listOf(version("root-v1", "root"))
            )
        )
        val issues = mutableListOf<QuickPlanningIssue>()

        val plan = runBlocking {
            planner.plan(
                request = request(),
                targets = listOf(
                    target("one", File("build/quick-planner/partial-one").canonicalFile),
                    target("two", File("build/quick-planner/partial-two").canonicalFile)
                        .copy(minecraftVersion = "1.21")
                ),
                onIssue = issues::add
            )
        }

        assertEquals(listOf("root-v1"), plan.map { it.file.versionId })
        assertEquals(listOf("one"), plan.single().targets.map { it.game.versionName })
        assertEquals(listOf("root" to "two"), issues.map { it.projectId to it.versionName })
    }

    @Test
    fun missingCompatibleRequiredDependencyIsSkippedAndReported() {
        val root = version("root-v1", "root", dependencies = listOf(dependency("dep")))
        val planner = planner(
            versions = mapOf(
                "MODRINTH/root" to listOf(root),
                "MODRINTH/dep" to listOf(version("dep-v1", "dep", games = arrayOf("1.21")))
            )
        )
        val issues = mutableListOf<QuickPlanningIssue>()

        val plan = runBlocking {
            planner.plan(request(), listOf(target()), onIssue = issues::add)
        }

        assertEquals(listOf("root-v1"), plan.map { it.file.versionId })
        assertEquals(listOf("dep" to "target"), issues.map { it.projectId to it.versionName })
    }

    @Test
    fun projectWithoutAnyVersionsIsSkippedAndReported() = runBlocking<Unit> {
        val issues = mutableListOf<QuickPlanningIssue>()

        val plan = planner(versions = emptyMap()).plan(
            request(), listOf(target()), onIssue = issues::add
        )

        assertEquals(0, plan.size)
        assertEquals(listOf("root" to "target"), issues.map { it.projectId to it.versionName })
    }

    @Test
    fun allUnavailableProjectVersionsFailWithoutCompatibilityIssue() {
        val issues = mutableListOf<QuickPlanningIssue>()
        val planner = planner(
            versions = mapOf(
                "MODRINTH/root" to listOf(version("root-v1", "root", strictInit = { false }))
            )
        )

        assertThrows(QuickPlanningException::class.java) {
            runBlocking { planner.plan(request(), listOf(target()), onIssue = issues::add) }
        }
        assertEquals(0, issues.size)
    }

    @Test
    fun unavailableVersionsAreIgnoredWhenOtherFilesCanInitialize() = runBlocking<Unit> {
        val planner = planner(
            versions = mapOf(
                "MODRINTH/root" to listOf(
                    version("root-new", "root", day = "2025-01-03", strictInit = { false }),
                    version("root-old", "root", day = "2025-01-02")
                )
            )
        )

        val plan = planner.plan(request(), listOf(target()))

        assertEquals(listOf("root-old"), plan.map { it.file.versionId })
    }

    @Test
    fun initializationFailureFailsWithoutCompatibilityIssue() {
        val issues = mutableListOf<QuickPlanningIssue>()
        val planner = planner(
            versions = mapOf(
                "MODRINTH/root" to listOf(
                    version("root-new", "root", strictInit = { throw IOException("offline") }),
                    version("root-old", "root", day = "2025-01-01")
                )
            )
        )

        assertThrows(QuickPlanningException::class.java) {
            runBlocking { planner.plan(request(), listOf(target()), onIssue = issues::add) }
        }
        assertEquals(0, issues.size)
    }

    @Test
    fun initializationCancellationIsNotConvertedToPlanningFailure() {
        val planner = planner(
            versions = mapOf(
                "MODRINTH/root" to listOf(
                    version("root-v1", "root", strictInit = { throw CancellationException("stop") })
                )
            )
        )

        assertThrows(CancellationException::class.java) {
            runBlocking { planner.plan(request(), listOf(target())) }
        }
    }

    @Test
    fun exactDependencyUsesStrictInitialization() {
        val pinned = version("dep-v1", "dep", strictInit = { throw IOException("offline") })
        val root = version(
            "root-v1", "root", dependencies = listOf(dependency("dep", versionId = "dep-v1"))
        )
        val planner = planner(
            versions = mapOf("MODRINTH/root" to listOf(root)),
            exact = mapOf("dep-v1" to pinned)
        )

        assertThrows(QuickPlanningException::class.java) {
            runBlocking { planner.plan(request(), listOf(target())) }
        }
    }

    private fun planner(
        versions: Map<String, List<PlatformVersion>>,
        exact: Map<String, PlatformVersion> = emptyMap(),
        installed: Set<String> = emptySet(),
        installedVersions: Set<String> = emptySet()
    ) = QuickDownloadPlanner(
        loadVersions = { projectId, platform -> versions["${platform.name}/$projectId"].orEmpty() },
        loadVersion = { versionId, _ -> exact[versionId] ?: error("Missing exact version $versionId") },
        loadProject = { projectId, platform -> FakeProject(projectId, platform) },
        installedChecker = { _, projectId, versionId, _ ->
            projectId in installed && (versionId == null || versionId in installedVersions)
        }
    )

    private fun request(classes: PlatformClasses = PlatformClasses.MOD) = QuickDownloadRequest(
        platform = Platform.MODRINTH,
        projectId = "root",
        title = "Root",
        classes = classes,
        summary = QuickCompatibilitySummary.Unknown
    )

    private fun target(
        name: String = "target",
        gameDir: File = File("build/quick-planner/game").canonicalFile
    ) = QuickGameTarget(name, gameDir, "1.20.1", listOf("Fabric"))

    private fun dependency(
        projectId: String,
        versionId: String? = null,
        type: PlatformDependencyType = PlatformDependencyType.REQUIRED
    ) = PlatformVersion.PlatformDependency(Platform.MODRINTH, projectId, versionId, type)

    private fun version(
        id: String,
        projectId: String,
        day: String = "2025-01-02",
        games: Array<String> = arrayOf("1.20.1"),
        dependencies: List<PlatformVersion.PlatformDependency> = emptyList(),
        loaders: Array<String> = arrayOf("Fabric"),
        fileName: String? = null,
        strictInit: () -> Boolean = { true }
    ): PlatformVersion = FakeVersion(id, projectId, day, games, dependencies, loaders, fileName, strictInit)
}

private class TestLabel(private val name: String) : PlatformDisplayLabel {
    override fun getDisplayName(): String = name
    override fun index(): Int = 0
}

private class FakeVersion(
    private val id: String,
    private val projectId: String,
    day: String,
    private val games: Array<String>,
    private val dependencies: List<PlatformVersion.PlatformDependency>,
    private val loaders: Array<String>,
    private val fileName: String?,
    private val strictInit: () -> Boolean
) : PlatformVersion {
    private val published = Instant.parse("${day}T00:00:00Z")

    override suspend fun initFile(currentProjectId: String): Boolean = true
    override suspend fun initFileStrict(currentProjectId: String): Boolean = strictInit()
    override fun platform(): Platform = Platform.MODRINTH
    override fun platformId(): String = id
    override fun platformProjectId(): String = projectId
    override fun platformDisplayName(): String = id
    override fun platformFileName(): String = fileName ?: "$id.jar"
    override fun platformGameVersion(): Array<String> = games
    override fun platformLoaders(): List<PlatformDisplayLabel> = loaders.map(::TestLabel)
    override fun platformReleaseType(): PlatformReleaseType = PlatformReleaseType.RELEASE
    override fun platformDependencies(): List<PlatformVersion.PlatformDependency> = dependencies
    override fun platformDownloadCount(): Long = 0L
    override fun platformDownloadUrl(): String = "https://example.invalid/$id.jar"
    override fun platformDatePublished(): Instant = published
    override fun platformSha1(): String? = null
    override fun platformFileSize(): Long = 1L
    override fun platformVersion(): String = id
}

private class FakeProject(
    private val id: String,
    private val ownerPlatform: Platform
) : PlatformProject {
    override fun platform(): Platform = ownerPlatform
    override fun platformId(): String = id
    override fun platformClasses(defaultClasses: PlatformClasses): PlatformClasses = PlatformClasses.MOD
    override fun platformSlug(): String = id
    override fun platformIconUrl(): String? = null
    override fun platformTitle(): String = id
    override fun platformSummary(): String = ""
    override fun platformAuthors(): List<String>? = null
    override fun platformDownloadCount(): Long = 0L
    override fun platformFollows(): Long? = null
    override fun platformModLoaders(): List<PlatformDisplayLabel> = listOf(TestLabel("Fabric"))
    override fun checkClasses() = Unit
    override fun platformCategories(classes: PlatformClasses) = null
    override fun platformUrls(defaultClasses: PlatformClasses): PlatformProject.Urls = PlatformProject.Urls()
    override fun platformScreenshots(): List<PlatformProject.Screenshot> = emptyList()
}
