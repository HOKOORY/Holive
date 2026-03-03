package com.ho.holive.domain.usecase

import com.ho.holive.core.common.AppResult
import com.ho.holive.data.remote.GithubApiService
import com.ho.holive.domain.model.AppUpdateInfo
import javax.inject.Inject

class CheckAppUpdateUseCase @Inject constructor(
    private val githubApiService: GithubApiService,
) {
    suspend operator fun invoke(
        currentVersionName: String,
        currentVersionCode: Int,
    ): AppResult<AppUpdateInfo?> {
        return try {
            val release = githubApiService.getLatestRelease(LATEST_RELEASE_URL)
            val latest = release.toUpdateInfo()
            val hasUpdate = isLatestNewer(
                latest = latest,
                currentVersionName = currentVersionName,
                currentVersionCode = currentVersionCode,
            )
            AppResult.Success(if (hasUpdate) latest else null)
        } catch (throwable: Throwable) {
            AppResult.Error(throwable)
        }
    }

    private fun com.ho.holive.data.remote.dto.GithubReleaseResponse.toUpdateInfo(): AppUpdateInfo {
        val normalizedTag = tagName.trim()
        val versionName = normalizedTag
            .removePrefix("v")
            .substringBefore("-build.")
            .ifBlank { normalizedTag.removePrefix("v") }
            .ifBlank { "latest" }
        val versionCode = Regex("""-build\.(\d+)$""")
            .find(normalizedTag)
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()
        return AppUpdateInfo(
            versionName = versionName,
            versionCode = versionCode,
            releaseUrl = htmlUrl.ifBlank { DEFAULT_RELEASE_WEB_URL },
        )
    }

    private fun isLatestNewer(
        latest: AppUpdateInfo,
        currentVersionName: String,
        currentVersionCode: Int,
    ): Boolean {
        val latestCode = latest.versionCode
        if (latestCode != null) {
            if (latestCode > currentVersionCode) return true
            if (latestCode < currentVersionCode) return false
        }
        return compareVersionName(latest.versionName, currentVersionName) > 0
    }

    private fun compareVersionName(left: String, right: String): Int {
        val leftParts = Regex("""\d+""").findAll(left).map { it.value.toInt() }.toList()
        val rightParts = Regex("""\d+""").findAll(right).map { it.value.toInt() }.toList()
        val maxSize = maxOf(leftParts.size, rightParts.size)
        for (index in 0 until maxSize) {
            val l = leftParts.getOrElse(index) { 0 }
            val r = rightParts.getOrElse(index) { 0 }
            if (l != r) return l.compareTo(r)
        }
        return 0
    }

    companion object {
        private const val LATEST_RELEASE_URL =
            "https://api.github.com/repos/HOKOORY/Holive/releases/latest"
        private const val DEFAULT_RELEASE_WEB_URL =
            "https://github.com/HOKOORY/Holive/releases/latest"
    }
}
