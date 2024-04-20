import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@Composable
fun App() {
    MaterialTheme {
        val birdsViewModel = getViewModel(Unit, viewModelFactory { BirdsViewModel() })

        /**
         * The UI state
         * TODO: how to ensure with lifeCycle so it doesn't collect when no more active?
         */
        val uiState by birdsViewModel.birdUiState.collectAsState()

        LaunchedEffect(birdsViewModel) {
            birdsViewModel.updateImages()
        }

        BirdsPage(
            birdUiState = uiState
        )
    }
}

@Composable
private fun BirdsPage(
    birdUiState: BirdUiState
) {
    AnimatedContent(
        targetState = birdUiState,
        label = "birdUiState"
    ) {
        when (it) {
            is BirdUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .background(Color.White)
                        .fillMaxSize()
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = "Loading..",
                        color = Color.Black
                    )
                }
            }
            is BirdUiState.Success -> {
                Box(
                    modifier = Modifier
                        .background(Color.White)
                        .fillMaxSize()
                ) {
                    Column {
                        Row {  }

                        AnimatedVisibility(it.list.isNotEmpty()) {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(180.dp),
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.fillMaxSize().padding(horizontal = 5.dp)
                            ) {
                                items(items = it.list) { birdImage: BirdImage ->
                                    BirdImageCell(birdImage)
                                }
                            }
                        }
                    }
                }
            }
            is BirdUiState.Error -> {
                Box(
                    modifier = Modifier
                        .background(Color.White)
                        .fillMaxSize()
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = it.msg,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun BirdImageCell(
    birdImage: BirdImage
) {
    KamelImage(
        resource = asyncPainterResource(data = "https://sebastianaigner.github.io/demo-image-api/${birdImage.imagePath}"),
        contentDescription = "${birdImage.category} by ${birdImage.author}",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.0F)
    )
}
