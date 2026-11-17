package eu.kanade.tachiyomi.animeextension.ar.witanime

import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.SEpisode
import eu.kanade.tachiyomi.animesource.model.Video
import eu.kanade.tachiyomi.animesource.online.ParsedAnimeHttpSource
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class Witanime : ParsedAnimeHttpSource() {

    override val name = "Witanime"

    override val baseUrl = "https://www.witanime.net"

    override val lang = "ar"

    override val supportsLatest = true

    override val client: OkHttpClient = network.cloudflareClient

    // =================- Popular Anime =================-
    override fun popularAnimeSelector(): String = "div.anime-card-container div.anime-card"

    override fun popularAnimeRequest(page: Int): Request {
        return Request.Builder("$baseUrl/anime-list?page=$page").build()
    }

    override fun popularAnimeFromElement(element: Element): SAnime {
        val anime = SAnime.create()
        anime.setUrlWithoutDomain(element.select("a.anime-card-poster").attr("href"))
        anime.title = element.select("div.anime-card-title h3").text()
        anime.thumbnail_url = element.select("img").attr("data-src").ifEmpty {
            element.select("img").attr("src")
        }
        return anime
    }

    override fun popularAnimeNextPageSelector(): String = "a.next.page-numbers"

    // =================- Latest Episodes =================-
    override fun latestUpdatesSelector(): String = "div.episodes-card-container div.col-item"

    override fun latestUpdatesRequest(page: Int): Request {
        return Request.Builder("$baseUrl/page/$page/").build()
    }

    override fun latestUpdatesFromElement(element: Element): SAnime {
        val anime = SAnime.create()
        val aTag = element.select("div.anime-card-title a").first() ?: element.select("a").first()
        anime.setUrlWithoutDomain(aTag.attr("href"))
        anime.title = aTag.text()
        anime.thumbnail_url = element.select("img").attr("data-src").ifEmpty {
            element.select("img").attr("src")
        }
        return anime
    }

    override fun latestUpdatesNextPageSelector(): String = "a.next.page-numbers"

    // =================- Search =================-
    override fun searchAnimeRequest(page: Int, query: String, filters: eu.kanade.tachiyomi.animesource.model.FilterList): Request {
        return Request.Builder("$baseUrl/?s=$query&page=$page").build()
    }

    override fun searchAnimeSelector(): String = popularAnimeSelector()

    override fun searchAnimeFromElement(element: Element): SAnime = popularAnimeFromElement(element)

    override fun searchAnimeNextPageSelector(): String = popularAnimeNextPageSelector()

    // =================- Anime Details =================-
    override fun animeDetailsParse(document: Document): SAnime {
        val anime = SAnime.create()
        anime.title = document.select("h1.anime-title").text()
        anime.genre = document.select("div.anime-genres a").joinToString(", ") { it.text() }
        anime.description = document.select("div.anime-story").text()
        anime.status = parseStatus(document.select("div.anime-status").text())
        anime.thumbnail_url = document.select("div.anime-poster img").attr("src")
        return anime
    }

    private fun parseStatus(statusStr: String): Int {
        return when {
            statusStr.contains("يعرض الان", true) || statusStr.contains("مستمر", true) -> SAnime.ONGOING
            statusStr.contains("مكتمل", true) -> SAnime.COMPLETED
            else -> SAnime.UNKNOWN
        }
    }

    // =================- Episodes =================-
    override fun episodeListSelector(): String = "div.episodes-list-content ul li, div.episodes-list div.episode-item"

    override fun episodeFromElement(element: Element): SEpisode {
        val episode = SEpisode.create()
        val aTag = element.select("a").first()!!
        episode.setUrlWithoutDomain(aTag.attr("href"))
        episode.name = aTag.text()
        return episode
    }

    // =================- Video Links / Servers =================-
    override fun videoListParse(response: Response): List<Video> {
        val document = response.asJsoup()
        val videoList = mutableListOf<Video>()
        val servers = document.select("ul.episodes-servers-list li, div.watch-servers-list div")
        
        for (server in servers) {
            val serverUrl = server.attr("data-url").ifEmpty { server.select("a").attr("href") }
            val serverName = server.text()
            if (serverUrl.isNotEmpty()) {
                videoList.add(Video(serverUrl, serverName, serverUrl))
            }
        }
        return videoList
    }

    override fun videoUrlParse(response: Response): String {
        throw UnsupportedOperationException("Not used.")
    }
}
