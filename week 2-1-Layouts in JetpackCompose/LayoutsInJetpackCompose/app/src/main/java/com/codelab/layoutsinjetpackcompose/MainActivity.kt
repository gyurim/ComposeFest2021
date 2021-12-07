package com.codelab.layoutsinjetpackcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import coil.compose.rememberImagePainter
import com.codelab.layoutsinjetpackcompose.ui.theme.LayoutsInJetpackComposeTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LayoutsInJetpackComposeTheme {
                LayoutsCodelab()
            }
        }
    }
}

@Composable
fun LayoutsCodelab() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "LayoutsCodelab")
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Favorite, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        BodyContent(
            Modifier
                .padding(innerPadding)
                .padding(8.dp))
    }
}

@Composable
fun SimpleList() {
    val scrollState = rememberScrollState()

    Column(Modifier.verticalScroll(scrollState)) {
        repeat(100) {
            Text("Item #$it")
        }
    }
}

@Composable
fun LazyList() {
    // 스크롤의 position 의 상태를 저장
    val scrollState = rememberLazyListState()

    LazyColumn(state = scrollState) {
        items(100) {
            Text("Item #$it")
        }
    }
}

@Composable
fun ImageList() {
    val scrollState = rememberLazyListState()

    LazyColumn(state = scrollState) {
        items(100) {
            ImageListItem(it)
        }
    }
}

@Composable
fun ImageListItem(index : Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = rememberImagePainter (
                data = "https://developer.android.com/images/brand/Android_Robot.png"
            ),
            contentDescription = "Android Logo",
            modifier = Modifier.size(50.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text("Item #$index", style = MaterialTheme.typography.subtitle1)
    }
}

@Composable
fun ScrollingList(){
    val listSize = 100
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column {
        Row {
            Button(onClick = {
                coroutineScope.launch {
                    scrollState.animateScrollToItem(0)
                }
            }) {
                Text("Scroll to the top")
            }

            Button(onClick = {
                coroutineScope.launch {
                    scrollState.animateScrollToItem(listSize - 1)
                }
            }) {
                Text("Scroll to the end")
            }
        }
    }

    LazyColumn(state = scrollState) {
        items(listSize) {
            ImageListItem(index = it)
        }
    }
}

fun Modifier.firstBaselineToTop(
    firstBaselineToTop: Dp
) = this.then(
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)

        // Check the composable has a first baseline
        check(placeable[FirstBaseline] != AlignmentLine.Unspecified)
        val firstBaseline = placeable[FirstBaseline]

        // Height of the composable with padding - first baseline
        val placeableY = firstBaselineToTop.roundToPx() - firstBaseline
        val height = placeable.height + placeableY
        layout(placeable.width, height) {
            placeable.placeRelative(0, placeableY)
        }
    }
)

@Preview
@Composable
fun TextWithPaddingToBaselinePreview() {
    LayoutsInJetpackComposeTheme {
        Text("Hi There!", Modifier.firstBaselineToTop(32.dp))
    }
}

@Preview
@Composable
fun TextWithNormalPaddingPreview() {
    LayoutsInJetpackComposeTheme {
        Text("Hi There!", Modifier.padding(top = 32.dp))
    }
}

@Composable
fun MyOwnColumn(
    modifier:Modifier = Modifier,
    // custom layout attributes
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        // measure and position children given constraints logic here
        val placeables = measurables.map {measurable ->
            measurable.measure(constraints)
        }

        var yPosition = 0

        // Set the size of the layout as big as it can
        layout(constraints.maxWidth, constraints.maxHeight) {
            // Place children in the parent layout
            placeables.forEach { placeable ->
                // Position item on the screen
                placeable.placeRelative(x = 0, y = yPosition)

                // Record the y co-ord placed up to
                yPosition += placeable.height
            }
        }
    }
}

@Composable
fun StaggeredGrid(
    modifier: Modifier = Modifier,
    rows: Int = 3,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        // measure and position children given constraints logic here

        // 각 row의 전체 width 저장 변수
        val rowWidths = IntArray(rows) {0}

        // 각 row의 최대 height 저장 변수
        val rowHeights = IntArray(rows) {0}

        // Don't constrain child view further, measure them with given constraints
        // List of measured children
        val placeables = measurables.mapIndexed {index, measurable ->
            // Measure each child
            val placeable = measurable.measure(constraints)

            //Track the width and max height of each row
            val row = index % rows

            // child elements의 각 width를 누적
            rowWidths[row] += placeable.width

            // 해당 row에 저장되는 child elements 중 최대 height를 갖는 값을 저장
            rowHeights[row] = Math.max(rowHeights[row], placeable.height)

            placeable
        }

        // Grid's width is the widest row
        val width = rowWidths.maxOrNull()
            ?.coerceIn(constraints.minWidth.rangeTo(constraints.maxWidth)) ?: constraints.minWidth
        // coerceIn : 값을 특정 범위로 한정

        // Grid's height is the sum of the tallest element of each row
        // coerced to the height constraints
        val height = rowHeights.sumOf {it}
            .coerceIn(constraints.minHeight.rangeTo(constraints.maxHeight))

        // 각 row 의 y position 을 저장. 각 row 의 y 이전 값은 이전 row 의 height 누적 값
        val rowY = IntArray(rows) {0}
        for (i in 1 until rows) {
            rowY[i] = rowY[i-1] + rowHeights[i-1]
        }

        // 3개의 row 중 가장 긴 width 와 3개의 row height 를 합쳐서 이 layout 의 크기를 확정시킨다.
        layout(width, height) {
            // 각 row 별로 child 의 width 를 누적하면서 child element 의 x 값으로 사용한다.
            val rowX = IntArray(rows) {0}
            placeables.forEachIndexed { index, placeable ->
                val row = index % rows

                placeable.placeRelative(
                    x = rowX[row],
                    y = rowY[row]
                )
                // placeRelative: 화면에 배치
                rowX[row] += placeable.width
            }
        }
    }
}

val topics = listOf(
    "Arts & Crafts", "Beauty", "Books", "Business", "Comics", "Culinary",
    "Design", "Fashion", "Film", "History", "Maths", "Music", "People", "Philosophy",
    "Religion", "Social sciences", "Technology", "TV", "Writing"
)

@Composable
fun Chip(modifier: Modifier = Modifier, text: String) {
    Card(
        modifier = modifier,
        border = BorderStroke(color = Color.Black, width = Dp.Hairline),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp, 16.dp)
                    .background(color = MaterialTheme.colors.secondary)
            )
            Spacer(Modifier.width(4.dp))
            Text(text = text)
        }
    }
}

@Stable
fun Modifier.padding(all: Dp) =
    this.then(
        PaddingModifier(start = all, top = all, end = all, bottom = all, rtlAware = true)
    )

private class PaddingModifier(
    val start: Dp = 0.dp,
    val top: Dp = 0.dp,
    val end: Dp = 0.dp,
    val bottom: Dp = 0.dp,
    val rtlAware: Boolean,
) : LayoutModifier {

    // measure 메서드를 재정의
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val horizontal = start.roundToPx() + end.roundToPx()
        val vertical = top.roundToPx() + bottom.roundToPx()

        val placeable = measurable.measure(constraints.offset(-horizontal, -vertical))

        // 요소의 새 width = 자식의 width + 요소의 너비 제약조건으로 강제 변환된 start와 end 패딩 값
        val width = constraints.constrainWidth(placeable.width + horizontal)
        // 요소의 새 height = 자식의 height + 요소의 높이 제약조건으로 강제 변환된 top과 bottom 패딩 값
        val height = constraints.constrainHeight(placeable.height + vertical)

        return layout(width, height) {
            if (rtlAware) {
                placeable.placeRelative(start.roundToPx(), top.roundToPx())
            } else {
                placeable.place(start.roundToPx(), top.roundToPx())
            }
        }
    }
}

@Composable
fun BodyContent(modifier: Modifier = Modifier) {

    // 함수 분리 & Modifier 를 param 으로 전달 -> 재사용성 & 테스트 용이
//    Column(modifier = modifier) {
//        Text(text = "Hi there")
//        Text(text = "Thanks for going through the Layouts codelab")
//        ScrollingList()
//    }

    // MyOwnColumn
//    MyOwnColumn(modifier.padding(8.dp)) {
//        Text("MyOwnColumn")
//        Text("places items")
//        Text("vertically")
//        Text("We've done it by hand!")
//    }

    // StaggeredGrid
//   Row(modifier = modifier.horizontalScroll(rememberScrollState())) {
//       StaggeredGrid {
//           for (topic in topics) {
//               Chip(modifier = Modifier.padding(8.dp), text = topic)
//           }
//       }
//   }

    // Padding Modifier
    Row(
        modifier = modifier
            .background(color = Color.LightGray)
            .padding(16.dp)
            .size(200.dp)
            .horizontalScroll(rememberScrollState())
    ) {
        StaggeredGrid {
            for (topic in topics) {
                Chip(modifier = Modifier.padding(8.dp), text = topic)
            }
        }
    }
}

@Composable
fun ConstraintLayoutContent() {
    ConstraintLayout {
        // Create references for the composables to constrain
        val (button1, button2, text) = createRefs()

        Button(
            onClick = {},
            // Button 컴포저블에 reference "button" 할당
            // ConstraintLayout 의 top에 제한
            modifier = Modifier.constrainAs(button1) {
                top.linkTo(parent.top, margin = 16.dp)
            }
        ) {
            Text("Button 1")
        }

        // Text 컴포저블에 reference "text 할당
        // Button 컴포저블의 bottom 에 제한
        Text("Text", Modifier.constrainAs(text) {
            top.linkTo(button1.bottom, margin = 16.dp)
            centerAround(button1.end)
        })

        // barrier 나 다른 helper 들은 ConstraintLayout 의 body 안에서 만들 수 있지만, constrainAs 내부에서 만들 수 없음
        val barrier = createEndBarrier(button1, text)
        Button(
            onClick = {},
            modifier = Modifier.constrainAs(button2) {
                top.linkTo(parent.top, margin = 16.dp)
                start.linkTo(barrier)
            }
        ) {
            Text("Button 2")
        }
    }
}

@Composable
fun LargeConstraintLayout() {
    ConstraintLayout {
        val text = createRef()

        val guideline = createGuidelineFromStart(fraction = 0.5f)
        Text(
            "This is a very very very very very very very long text",
            Modifier.constrainAs(text) {
                linkTo(start = guideline, end = parent.end)
                width = Dimension.preferredWrapContent
            }
        )
    }
}

@Composable
fun DecoupledConstraintLayout() {
    BoxWithConstraints {
        val constraints = if (maxWidth < maxHeight) {
            decoupledConstraints(margin = 16.dp) // Portrait constraints
        } else {
            decoupledConstraints(margin = 32.dp) // Landscape constraints
        }

        ConstraintLayout(constraints) {
            Button(
                onClick = {},
                modifier = Modifier.layoutId("button")
            ) {
                Text("Button")
            }
            Text("Text", Modifier.layoutId("text"))
        }
    }
}

private fun decoupledConstraints(margin: Dp): ConstraintSet {
    return ConstraintSet {
        val button = createRefFor("button")
        val text = createRefFor("text")

        constrain(button) {
            top.linkTo(parent.top, margin = margin)
        }
        constrain(text) {
            top.linkTo(button.bottom, margin)
        }
    }
}

@Composable
fun TwoTexts(modifier: Modifier = Modifier, text1: String, text2: String) {
    // Row 의 minIntrinsicHeight 는 자식의 최대 minIntrinsicHeight 가 됨
    Row (modifier = modifier.height(IntrinsicSize.Min)) {
        Text (
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
                .wrapContentWidth(Alignment.Start),
            text = text1
        )

        Divider(color = Color.Black, modifier = Modifier.fillMaxHeight().width(1.dp))
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp)
                .wrapContentWidth(Alignment.End),
            text = text2
        )
    }
}

@Preview
@Composable
fun TwoTextsPreview() {
    LayoutsInJetpackComposeTheme {
        Surface {
            TwoTexts(text1 = "Hi", text2 = "there")
        }
    }
}

@Preview
@Composable
fun ChipPreview() {
    LayoutsInJetpackComposeTheme {
        Chip(text = "Hi there")
    }
}

@Preview
@Composable
fun LayoutsCodelabPreview() {
    LayoutsInJetpackComposeTheme {
//        LayoutsCodelab()
//        ConstraintLayoutContent()
//        LargeConstraintLayout()
        DecoupledConstraintLayout()
    }
}
