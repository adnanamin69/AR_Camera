package com.adnan.arcamera

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adnan.arcamera.ui.theme.ARMenuTheme
import com.adnan.arcamera.ui.theme.Translucent
import com.google.android.filament.Engine
import com.google.ar.core.Config
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.ar.node.PlacementMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ARMenuTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val currentModel = remember {
                            mutableStateOf("wall_4")
                        }
                        ARScreen(currentModel.value)
                        WallArts(
                            modifier = Modifier.align(Alignment.BottomCenter),
                        ) {
                            currentModel.value = it
                        }

                        /* Menu(modifier = Modifier.align(Alignment.BottomCenter)) {
                             currentModel.value = it
                         }*/

                    }
                }
            }
        }
    }
}


@Composable
fun SquareImageImage(
    modifier: Modifier = Modifier,
    imageId: Int, onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(width = 3.dp, Translucent, RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = imageId),
            contentDescription = null,
            contentScale = ContentScale.FillBounds
        )
    }
}

@Composable
fun ARScreen(model: String) {
    val nodes = remember {
        mutableListOf<ArNode>()
    }
    val modelNode = remember {
        mutableStateOf<ArModelNode?>(null)
    }
    var engine by remember {
        mutableStateOf<Engine?>(null)
    }


    val placeModelButton = remember {
        mutableStateOf(false)
    }
    Box(modifier = Modifier.fillMaxSize()) {
        ARScene(
            modifier = Modifier.fillMaxSize(),
            nodes = nodes,
            planeRenderer = true,
            onCreate = { arSceneView ->
                arSceneView.lightEstimationMode = Config.LightEstimationMode.DISABLED
                arSceneView.planeRenderer.isShadowReceiver = false
                engine = arSceneView.engine
                val x = ArModelNode(arSceneView.engine, PlacementMode.INSTANT).apply {
                    loadModelGlbAsync(
                        glbFileLocation = "models/${model}.glb",
                        scaleToUnits = 0.8f
                    ) {

                    }
                    onAnchorChanged = {
                        placeModelButton.value = !isAnchored
                    }
                    onHitResult = { node, hitResult ->
                        placeModelButton.value = node.isTracking
                    }

                }

                modelNode.value = x
                nodes.add(x)

            },
            onSessionCreate = {
                planeRenderer.isVisible = false
            }
        )
        OutlinedButton(

            onClick = {
                modelNode.value?.anchor()
            }, modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        ) {
            Text(text = "Place")
        }
    }



    LaunchedEffect(key1 = model) {

        if (modelNode.value?.isAnchored == false)
            modelNode.value?.loadModelGlbAsync(
                glbFileLocation = "models/${model}.glb",
                scaleToUnits = 0.8f
            )


        if (modelNode.value?.isAnchored == true || modelNode.value == null)
            engine?.let {
                val x = ArModelNode(it, PlacementMode.INSTANT).apply {
                    loadModelGlbAsync(
                        glbFileLocation = "models/${model}.glb",
                        scaleToUnits = 0.8f
                    ) {

                    }
                    onAnchorChanged = {
                        placeModelButton.value = !isAnchored
                    }
                    onHitResult = { node, hitResult ->
                        placeModelButton.value = node.isTracking
                    }

                }

                modelNode.value = x
                nodes.add(x)
            }

        Log.e("errorloading", "ERROR LOADING MODEL")
    }

}


@Composable
fun WallArts(modifier: Modifier = Modifier, onClick: (String) -> Unit) {
    val itemsList = listOf(
        Items("new_1", R.drawable.new_1, "models/burger.glb"),
        Items("new_2", R.drawable.new_2, "models/instant.glb"),
        Items("new_3", R.drawable.new_3, "models/momos.glb"),
        Items("new_4", R.drawable.new_4, "models/pizza.glb"),
        Items("new_5", R.drawable.new_5, "models/ramen.glb"),
        Items("new_6", R.drawable.new_6, "models/ramen.glb"),

        )

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        content = {
            items(itemsList, key = {
                it.name
            }) {
                SquareImageImage(imageId = it.imageId) {
                    onClick.invoke(it.name)
                }
            }
        })
}


data class Items(var name: String, var imageId: Int, val path: String)


@Preview(showBackground = false)
@Composable
fun DeviceListPreview() {
    WallArts() {}
}


