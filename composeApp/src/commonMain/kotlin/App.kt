import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

                        /**
                         * To make sure the isClicked UI state stays with the item after recomposition and filtering
                         */
                        val listStatePair = remember {
                            it.list.map { birdImage ->
                                birdImage to mutableStateOf(false)
                            }
                        }

                        //Content
                        AnimatedVisibility(listStatePair.isNotEmpty()) {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(180.dp),
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.fillMaxSize().padding(horizontal = 5.dp)
                            ) {

                                items(
                                    items = listStatePair
                                        .filter { birdImagePair ->
                                            (birdImagePair.first.category ?: "Unknown")
                                                .contains(
                                                    other = currentSelectedCat,
                                                    ignoreCase = true
                                                )
                                        }
                                ) { birdImagePair ->
                                    BirdImageCell(
                                        birdImage = birdImagePair.first,
                                        isClicked = birdImagePair.second
                                    )
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
    birdImage: BirdImage,
    isClicked: MutableState<Boolean>
) {
    Box {
        KamelImage(
            resource = asyncPainterResource(data = "https://sebastianaigner.github.io/demo-image-api/${birdImage.imagePath}"),
            contentDescription = "${birdImage.category} by ${birdImage.author}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.0F)
                .clickable { isClicked.value = !isClicked.value }
        )
        AnimatedVisibility(
            modifier = Modifier
                .matchParentSize(),
            enter = fadeIn(),
            exit = fadeOut(),
            visible = isClicked.value
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black,
                                Color.Transparent,
                                Color.Black
                            )
                        )
                    )
            ) {
                Text(
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    text = birdImage.category ?: "Unknown",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Text(
                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                    text = "by ${birdImage.author ?: "Unknown"}",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}
