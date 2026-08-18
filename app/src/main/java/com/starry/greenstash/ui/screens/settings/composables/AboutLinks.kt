package com.starry.greenstash.ui.screens.settings.composables

sealed class AboutLinks(val url: String) {
    data object ReadMe : AboutLinks("https://github.com/apexspace9-a11y/GreenStash")
    data object Website : AboutLinks("https://github.com/apexspace9-a11y/GreenStash")
    data object PrivacyPolicy : AboutLinks(
        "https://github.com/apexspace9-a11y/GreenStash/blob/main/legal/PRIVACY-POLICY.md"
    )
    data object GithubIssues : AboutLinks("https://github.com/apexspace9-a11y/GreenStash/issues")
    data object Telegram : AboutLinks("https://github.com/apexspace9-a11y/GreenStash/discussions")
    data object Sponsor : AboutLinks("https://github.com/apexspace9-a11y/GreenStash")
}
