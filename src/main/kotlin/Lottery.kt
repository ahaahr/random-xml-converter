data class Lottery(
        val name: String = "",
        val ticketFormats: List<TicketFormat>,
        val jurisdictions: List<Jurisdiction>,
        val url: String?,
        val drawDays: List<String>?,
        val affiliateLinks: List<AffiliateLink>
)

data class TicketFormat(
        val n: Int,
        val length: List<Int>,
        val min: List<Int>,
        val max: List<Int>,
        val replacement: List<Boolean>
)

data class Jurisdiction(
        val country: String,
        val region: String?
)

data class AffiliateLink(
        val name: String,
        val url: String?
)
