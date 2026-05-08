package site.doramusic.app.score

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dora.widget.DoraPokerView
import site.doramusic.app.R

class GalleryCardAdapter(
    private val cards: MutableList<GalleryCard>,
    private val getCardImage: (Int) -> Int,   // 根据 card.number 获取正面图片
    private val onCardDraw: (GalleryCard, DoraPokerView) -> Unit
) : RecyclerView.Adapter<GalleryCardAdapter.CardViewHolder>() {

    // 记录已经翻开的卡片编号
    private val flippedCards = mutableSetOf<Int>()

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pokerView: DoraPokerView = itemView.findViewById(R.id.pokerView)

        fun bind(card: GalleryCard) {
            // 如果已翻开，则显示正面
            if (card.isDrawn || flippedCards.contains(card.number)) {
                pokerView.setFrontImage(getCardImage(card.number))
                pokerView.setBackImage(getCardImage(card.number))
            } else {
                // 背面显示
                pokerView.setFrontImage(getCardImage(card.number))
                pokerView.setBackImage(R.drawable.card_back)
            }

            pokerView.reset()

            pokerView.setOnClickListener {
                // 翻回背面再翻出正面
                pokerView.reset()
                flippedCards.add(card.number)
                onCardDraw(card, pokerView)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gallery_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(cards[position])
    }

    override fun getItemCount(): Int = cards.size

    /**
     * 通过编号更新某张卡。
     */
    fun updateCard(cardNumber: Int, updatedCard: GalleryCard) {
        if (cardNumber in 0 until cards.size) {
            cards[cardNumber] = updatedCard
            notifyItemChanged(cardNumber)
        }
    }

    /**
     * 重置所有翻开的卡状态。
     */
    fun resetFlipped() {
        flippedCards.clear()
        notifyDataSetChanged()
    }
}
