import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BirdImage(
    @SerialName("category") val category: String?,
    @SerialName("path") val imagePath: String?,
    @SerialName("author") val author: String?,
)
