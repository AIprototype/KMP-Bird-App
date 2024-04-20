import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

                /**
                 * To apply category based images
                 */
                var currentSelectedCat by remember { mutableStateOf("") }

                Box(
                    modifier = Modifier
                        .background(Color.White)
                        .fillMaxSize()
                ) {
                    Column {

                        Spacer(modifier = Modifier.height(8.dp))

                        //Tabs
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            it.categories.map { category: String? ->

                                val bgColor by animateColorAsState(
                                    targetValue = if (currentSelectedCat == category) {
                                        Color.White
                                    } else {
                                        Color.Black
                                    }
                                )

                                val textColor by animateColorAsState(
                                    targetValue = if (currentSelectedCat == category) {
                                        Color.Black
                                    } else {
                                        Color.White
                                    }
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1F)
                                        .background(
                                            color = bgColor,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable {
                                            //If same clicked twice, it unselects it
                                            currentSelectedCat =
                                                if (currentSelectedCat == (category ?: "Unknown")) {
                                                    ""
                                                } else {
                                                    category ?: "Unknown"
                                                }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        modifier = Modifier
                                            .padding(
                                                horizontal = 4.dp,
                                                vertical = 8.dp
                                            ),
                                        text = category ?: "Unknown",
                                        color = textColor
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        //Content
                        AnimatedVisibility(it.list.isNotEmpty()) {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(180.dp),
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.fillMaxSize().padding(horizontal = 5.dp)
                            ) {
                                items(items = it.list.filter { birdImage ->
                                    (birdImage.category ?: "Unknown")
                                        .contains(
                                            other = currentSelectedCat,
                                            ignoreCase = true
                                        )
                                }) { birdImage: BirdImage ->
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
