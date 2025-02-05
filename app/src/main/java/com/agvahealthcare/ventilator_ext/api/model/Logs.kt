import com.google.gson.annotations.SerializedName

data class Logs (

	@SerializedName("_id") val _id : String,
	@SerializedName("did") val did : String,
	@SerializedName("logGeneratedDate") val logGeneratedDate : String,
	@SerializedName("logMsg") val logMsg : String,
	@SerializedName("device_types") val device_types : String,
	@SerializedName("logType") val logType : String,
	@SerializedName("createdAt") val createdAt : String,
	@SerializedName("updatedAt") val updatedAt : String,
	@SerializedName("__v") val __v : Int
)