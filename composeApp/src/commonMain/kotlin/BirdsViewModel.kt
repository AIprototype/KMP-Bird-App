import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface BirdUiState {
    data object Loading: BirdUiState
    data class Success(val list: List<BirdImage>, val categories: List<String?>): BirdUiState
    data class Error(val msg: String): BirdUiState
}

class BirdsViewModel: ViewModel() {

    private val _birdUiState = MutableStateFlow<BirdUiState>(BirdUiState.Loading)
    val birdUiState = _birdUiState.asStateFlow()

    private val httpClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    /**
     * Call to get the Bird Images, updates to [birdUiState]
     */
    fun updateImages() {
        viewModelScope.launch {
            _birdUiState.update { BirdUiState.Loading }
            try {
                val images = getImages()

                val uniqueCategories = images.distinctBy {
                    it.category
                }.map {
                    it.category
                }

                _birdUiState.update { BirdUiState.Success(
                    list = images,
                    categories = uniqueCategories
                ) }
            } catch (e: Exception) {
                _birdUiState.update { BirdUiState.Error(msg = e.message ?: "Some error happened") }
            }
        }
    }

    private suspend fun getImages(): List<BirdImage> =
        httpClient.get(urlString = "https://sebi.io/demo-image-api/pictures.json")
            .body<List<BirdImage>>()

    override fun onCleared() {
        super.onCleared()
        httpClient.close()
    }
}
