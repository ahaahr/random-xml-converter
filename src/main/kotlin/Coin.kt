data class Category(
        val categoryName: String,
        val categoryCode: String?,
        val obsolete: Boolean?,
        val coins: List<Coin>
)

data class Coin(
        val region: String?,
        val year: Int?,
        val name: String?,
        val denomination: String?,
        val collectible: Boolean,
        val imageObverse: String,
        val imageReverse: String,
        val acknowledgementUrl: String?,
        val acknowledgementText: String?
)
