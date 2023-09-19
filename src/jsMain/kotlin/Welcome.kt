import react.FC
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.p


val Welcome = FC<Props> { _ ->
	div {
		h1 {
			+"Hey!"
		}
		h2 {
			+"Please don't use this in your app, if you do you are stealing api bandwidth with MetaDeck."
		}
		p {
			+"However, if you do intend to use it, please ask over on the Decky Loader discord for permission, in the MetaDeck support thread. The api docs are at "
			a { href = "https://api.emudeck.com/metadeck/api/docs"
				+"https://api.emudedeck.com/metadeck/api/docs"
			}
			+" if you wish to know how to use it, but please be mindful of the igdb api request rate limits."
		}
	}
}