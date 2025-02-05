import com.google.gson.annotations.SerializedName

data class Data (

	@SerializedName("count") val count : Int,
	@SerializedName("logs") val logs : List<Logs>
)