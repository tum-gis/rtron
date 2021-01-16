package io.rtron.documentation

import com.eden.orchid.api.OrchidContext
import com.eden.orchid.api.options.annotations.Option
import com.eden.orchid.api.registration.OrchidModule
import com.eden.orchid.api.resources.resourcesource.DelegatingResourceSource
import com.eden.orchid.api.resources.resourcesource.OrchidResourceSource
import com.eden.orchid.api.resources.resourcesource.ThemeResourceSource
import com.eden.orchid.api.theme.Theme
import com.eden.orchid.api.theme.assets.AssetManagerDelegate
import com.eden.orchid.api.theme.models.Social
import com.eden.orchid.copper.CopperTheme
import com.eden.orchid.utilities.OrchidUtils.DEFAULT_PRIORITY
import com.eden.orchid.utilities.addToSet
import javax.inject.Inject

class RtronModule : OrchidModule() {

    override fun configure() {
        addToSet<Theme, RtronTheme>()
    }
}

class RtronTheme
@Inject
constructor(context: OrchidContext) : Theme(context, "RtronTheme", DEFAULT_PRIORITY + 1) {

    private val delegateTheme = CopperTheme(context)

    @Option
    lateinit var social: Social

    override fun loadAssets(delegate: AssetManagerDelegate): Unit = with(delegate) {
        addCss("assets/css/bulma.min.css")
        addCss("assets/css/bulma-tooltip.css")
        addCss("assets/css/bulma-accordion.min.css")

        addJs("https://use.fontawesome.com/releases/v5.4.0/js/all.js") {
            defer = true
            attrs["data-search-pseudo-elements"] = "true"
        }
        addJs("assets/js/bulma.js")
        addJs("assets/js/bulma-accordion.min.js")
        addJs("assets/js/bulma-tabs.js")
    }

    override fun getResourceSource(): OrchidResourceSource {
        return DelegatingResourceSource(
            listOfNotNull(super.getResourceSource(), delegateTheme.resourceSource),
            emptyList(),
            priority,
            ThemeResourceSource
        )
    }
}
