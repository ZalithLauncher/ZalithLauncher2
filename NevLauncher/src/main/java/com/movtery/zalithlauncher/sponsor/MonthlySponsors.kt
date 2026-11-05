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

package com.movtery.zalithlauncher.sponsor

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 月度赞助数据
 */
@Serializable
data class MonthlySponsors(
    /** 数据所属月份，格式 YYYY-MM，北京时间 */
    @SerialName("month")
    val month: String,
    /** 赞助条目，按时间从新到旧排序 */
    @SerialName("sponsors")
    val sponsors: List<Sponsor>,
    /** 上月结算数据块 */
    @SerialName("last_month")
    val lastMonth: LastMonthSponsors
)

/**
 * 上月结算数据
 */
@Serializable
data class LastMonthSponsors(
    /** 结算月份标识，格式 YYYY-MM，北京时间 */
    @SerialName("month")
    val month: String,
    /** 上月赞助条目，上月无赞助时为空数组 */
    @SerialName("list")
    val list: List<Sponsor> = emptyList()
)

/**
 * 单笔赞助条目
 */
@Serializable
data class Sponsor(
    @SerialName("user")
    val user: SponsorUser,
    /** 赞助时间，格式 YYYY/MM/DD，北京时间 */
    @SerialName("time")
    val time: String,
    @SerialName("plan")
    val plan: SponsorPlan
)

/**
 * 赞助者信息
 */
@Serializable
data class SponsorUser(
    /** 赞助者名称，查询不到时为“匿名赞助者” */
    @SerialName("name")
    val name: String,
    /** 赞助者头像 URL，查询不到时为空串 */
    @SerialName("avatar")
    val avatar: String
)

/**
 * 赞助方案信息
 */
@Serializable
data class SponsorPlan(
    /** 方案 ID，自选金额赞助为空串 */
    @SerialName("id")
    val id: String,
    /** 方案名称，查询失败时为空串；自选金额为“自选金额” */
    @SerialName("name")
    val name: String,
    /** 方案标价（方案级单价），自选金额订单为实付金额，方案查询失败时为 null */
    @SerialName("price")
    val price: Double? = null,
    /** 实付金额（整笔订单），兑换码订单为 0.00 */
    @SerialName("total_amount")
    val totalAmount: Double,
    /** 折前金额（整笔订单），有折扣时大于实付 */
    @SerialName("show_amount")
    val showAmount: Double,
    /** 数量，订阅类为购买月数，商品类为购买件数 */
    val count: Int = 1
)
