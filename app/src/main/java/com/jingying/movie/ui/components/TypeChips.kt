package com.jingying.movie.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.jingying.movie.domain.model.MovieType
import com.jingying.movie.ui.theme.AccentRed
import com.jingying.movie.ui.theme.CardBackground
import com.jingying.movie.ui.theme.PrimaryText
import com.jingying.movie.ui.theme.White

@Composable
fun TypeChips(
    types: List<MovieType>,
    selectedTypeId: Int,
    onTypeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(types, key = { it.typeId }) { type ->
            val selected = type.typeId == selectedTypeId
            Text(
                text = type.typeName,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) White else PrimaryText,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (selected) AccentRed else CardBackground)
                    .clickable { onTypeSelected(type.typeId) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }
    }
}
