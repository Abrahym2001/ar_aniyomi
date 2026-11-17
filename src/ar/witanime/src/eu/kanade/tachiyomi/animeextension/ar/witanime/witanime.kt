package eu.kanade.tachiyomi.animeextension.ar.witanime

import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.SEpisode
import eu.kanade.tachiyomi.animesource.model.Video
import eu.kanade.tachiyomi.animesource.online.ParsedAnimeHttpSource
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class Witanime : ParsedAnimeHttpSource() {

    override val name = "Witanime"

    override val baseUrl = "https://witanime.io"

    override val lang = "ar"

    override val supportsLatest = true

    // Popular
    override fun popularAnimeSelector(): String = "div.anime-card-container div.anime-card"

    override fun popularAnimeRequest(page: Int) = okhttp3.Request.Builder().url("$baseUrl/anime-list?page=$page").build()

    override fun popularAnimeFromElement(element: Element): SAnime {
        val anime = SAnime.create()
        anime.setUrlWithoutDomain(element.select("a").attr("href"))
        anime.title = element.select("h3, .anime-card-title").text()
        anime.thumbnail_url = element.select("img").attr("data-src").ifEmpty { element.select("img").attr("src") }
        return anime
    }

    override fun popularAnimeNextPageSelector(): String = "a.next.page-numbers"

    // Latest
    override fun latestUpdatesSelector(): String = popularAnimeSelector()

    override fun latestUpdatesRequest(page: Int) = okhttp3.Request.Builder().url("$baseUrl/episodes?page=$page").build()

    override fun latestUpdatesFromElement(element: Element): SAnime = popularAnimeFromElement(element)

    override fun latestUpdatesNextPageSelector(): String = popularAnimeNextPageSelector()

    // Search
    override fun searchAnimeSelector(): String = popularAnimeSelector()

    override fun searchAnimeRequest(page: Int, query: String, filters: okhttp3.HttpUrl.Builder): okhttp3.Request {
        val url = "$baseUrl/search?q=$query&page=$page"
        return okhttp3.Request.Builder().url(url).build()
    }

    override fun searchAnimeFromElement(element: Element): SAnime = popularAnimeFromElement(element)

    override fun searchAnimeNextPageSelector(): String = popularAnimeNextPageSelector()

    // Details
    override fun animeDetailsParse(document: Document): SAnime {
        val anime = SAnime.create()
        anime.title = document.select("h1.anime-title").text()
        anime.description = document.select("div.anime-story, .story").text()
        anime.genre = document.select("div.anime-genres a").joinToString(", ") { it.text() }
        anime.status = SAnime.UNKNOWN
        return anime
    }

    // Episodes
    override fun episodeListSelector(): String = "ul.episodes-list li a, div.episodes-list-content a"

    override fun episodeFromElement(element: Element): SEpisode {
        val episode = SEpisode.create()
        episode.setUrlWithoutDomain(element.attr("href"))
        episode.name = element.text()
        return episode
    }

    // Video
    override fun videoListParse(response: Response): List<Video> {
        val document = response.asJsoup()
        val videoList = mutableListOf<Video>()
        val servers = document.select("ul.episodes-servers-list li, div.watch-servers-list div")
        for (server in servers) {
            val url = server.attr("data-video-url").ifEmpty { server.select("a").attr("href") }
            if (url.isNotEmpty()) {
                videoList.add(Video(url, "Server", url))
            }
        }
        return videoList
    }

    override fun videoUrlParse(document: Document) = throw Exception("Not used")
}
