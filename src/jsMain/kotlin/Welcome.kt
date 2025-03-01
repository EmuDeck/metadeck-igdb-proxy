import react.FC
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.br
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.strong


val Welcome = FC<Props> { _ ->
	div {
		h1 {
			+"Hey!"
		}
		h2 {
			+"This is an API server for MetaDeck's IGDB provider"
		}
		p {
			+"It proxys requests and queues them so that the api limit is not overflowed"; br()
			+"As such, the rate limit is shared with "; strong { +"all users on the server" }; br()
			br()
			+"If this is a community server, please don't use this server for projects other then MetaDeck, as it will prevent others from using the server legitimately"; br()
			+"If you have a large game library, I recommend you host your own server on your own local network to minimize latency and take the strain off the community servers"; br()
			+"If you are willing to host a community API server, reach out on the "; a { href = "https://discord.gg/bhVvDkMT7s"; +"discord" }; +" and I will add your server to the list after a review process"; br()
			br()
			+"API Docs can be found "; a { href = "/docs"; +"here" }
		}
	}
}