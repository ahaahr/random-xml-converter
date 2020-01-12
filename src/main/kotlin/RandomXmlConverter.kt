import com.google.gson.GsonBuilder
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.File
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

fun main(args: Array<String>) {
    println(getLotteryJson(readXml("data/lotteries.xml")))
    println(getCoinJson(readXml("data/coins.xml")))
}

fun readXml(filePath: String): Document {
    val xmlFile = File(filePath)

    val dbFactory = DocumentBuilderFactory.newInstance()
    val dBuilder = dbFactory.newDocumentBuilder()
    val xmlInput = InputSource(StringReader(xmlFile.readText()))
    return dBuilder.parse(xmlInput)
}

/**
 * Note that the Quick Draw lotteries wont be converted correctly using this function.
 * They will need to be converted by hand afterwards.
 */
fun getLotteryJson(doc: Document): String {
    val xPath = XPathFactory.newInstance().newXPath()

    val lotteryPath = "lotteries/lottery"

    val lotteries = xPath.evaluate(lotteryPath, doc, XPathConstants.NODESET) as NodeList

    println("lottery count: " + lotteries.length)

    val lotteryList = mutableListOf<Lottery>()
    for (lotteryIndex in 0 until lotteries.length) {
        val name = xPath.evaluate("/lotteries/lottery[$lotteryIndex + 1]/name", doc)

        val configuration = xPath.evaluate("/lotteries/lottery[$lotteryIndex + 1]/configuration", doc)
        val ticketFormats = mutableListOf<TicketFormat>()
        val regularCount = configuration.split(".").first().split("x").first().toInt()
        val regularMax = configuration.split(".").first().split("x")[1].toInt()
        val extraCount = configuration.split(".")[1].split("x").first().toInt()
        val extraMax = configuration.split(".")[1].split("x")[1].toInt()
        val n = if (extraCount < 0) 1 else 2
        if (n == 1) {
            ticketFormats.add(TicketFormat(1, listOf(regularCount), listOf(1), listOf(regularMax), listOf(false)))
        } else {
            ticketFormats.add(TicketFormat(2, listOf(regularCount, extraCount), listOf(1, 1), listOf(regularMax, extraMax), listOf(false, false)))
        }

        val codes = xPath.evaluate("/lotteries/lottery[$lotteryIndex + 1]/code", doc, XPathConstants.NODESET) as NodeList
        val jurisdictions = mutableListOf<Jurisdiction>()
        for (countryIndex in 0 until codes.length) {
            jurisdictions.add(Jurisdiction(xPath.evaluate("/lotteries/lottery[$lotteryIndex + 1]/country", doc), null))
        }

        val urlData = xPath.evaluate("/lotteries/lottery[$lotteryIndex + 1]/url", doc)
        val url = urlData.orNull()

        val lottery = Lottery(name, ticketFormats, jurisdictions, url, emptyList(), emptyList())
        lotteryList.add(lottery)
    }

    return GsonBuilder().serializeNulls().create().toJson(lotteryList)
}

fun getCoinJson(doc: Document): String {
    val xPath = XPathFactory.newInstance().newXPath()

    val categoryPath = "coins/category"

    val categories = xPath.evaluate(categoryPath, doc, XPathConstants.NODESET) as NodeList

    println("coin category count: " + categories.length)

    val categoryList = mutableListOf<Category>()
    for (categoryIndex in 0 until categories.length) {
        val categoryName = xPath.evaluate("/coins/category[$categoryIndex + 1]/@name", doc)

        val categoryCode = xPath.evaluate("/coins/category[$categoryIndex + 1]/@code", doc)

        val obsolete = xPath.evaluate("/coins/category[$categoryIndex + 1]/@obsolete", doc)

        val coins = xPath.evaluate("/coins/category[$categoryIndex + 1]/coin", doc, XPathConstants.NODESET) as NodeList
        val coinList = mutableListOf<Coin>()
        for (coinIndex in 0 until coins.length) {
            val region = xPath.evaluate("/coins/category[$categoryIndex + 1]/coin[$coinIndex + 1]/region", doc)

            val year = xPath.evaluate("/coins/category[$categoryIndex + 1]/coin[$coinIndex + 1]/year", doc)

            val coinName = xPath.evaluate("/coins/category[$categoryIndex + 1]/coin[$coinIndex + 1]/name", doc)

            val denomination = xPath.evaluate("/coins/category[$categoryIndex + 1]/coin[$coinIndex + 1]/denomination", doc)

            val collectible = xPath.evaluate("/coins/category[$categoryIndex + 1]/coin[$coinIndex + 1]/@collectible", doc)

            val obverse = xPath.evaluate("/coins/category[$categoryIndex + 1]/coin[$coinIndex + 1]/obverse", doc)

            val reverse = xPath.evaluate("/coins/category[$categoryIndex + 1]/coin[$coinIndex + 1]/reverse", doc)

            val acknowledgement = xPath.evaluate("/coins/category[$categoryIndex + 1]/coin[$coinIndex + 1]/acknowledgement", doc)

            coinList.add(Coin(
                    region = region.orNull(),
                    year = year.asIntOrNull(),
                    name = coinName.orNull(),
                    denomination = denomination,
                    collectible = collectible.isCollectible(),
                    imageObverse = obverse,
                    imageReverse = reverse,
                    acknowledgementUrl = null,
                    acknowledgementText = acknowledgement.orNull()
            ))
        }
        categoryList.add(Category(
                categoryName = categoryName,
                categoryCode = categoryCode.orNull(),
                obsolete = obsolete.isObsolete(),
                coins = coinList
        ))
    }

    return GsonBuilder().serializeNulls().create().toJson(categoryList)
}

fun String.orNull(): String? {
    return if (isNullOrBlank()) null else this
}

fun String.asIntOrNull(): Int? {
    return if (isNullOrBlank()) null else this.toInt()
}

private fun String.isCollectible(): Boolean {
    return !isNullOrBlank() && this == "collectible"
}

private fun String.isObsolete(): Boolean? {
    return if(!isNullOrBlank() && this == "obsolete") true else null
}
