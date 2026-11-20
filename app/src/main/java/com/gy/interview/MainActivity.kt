package com.gy.interview

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var tvCountdownDays: TextView
    private lateinit var tvTargetDate: TextView
    private lateinit var tvMastered: TextView
    private lateinit var tvCardTag: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var tvAnswer: TextView
    private lateinit var btnReveal: Button
    private lateinit var btnMarkMastered: Button
    private lateinit var progressBar: ProgressBar

    private var index = 0
    private var answerVisible = false

    private val prefs by lazy { getSharedPreferences("interview_prep", Context.MODE_PRIVATE) }

    /** Positions of cards the candidate has marked as mastered. */
    private val mastered: MutableSet<Int> by lazy {
        prefs.getStringSet(KEY_MASTERED, emptySet())!!
            .mapNotNull { it.toIntOrNull() }
            .toMutableSet()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        renderCountdown()
        renderCard()
        setupSwipe()
        setupButtons()
    }

    // ---------------------------------------------------------------- views

    private fun bindViews() {
        tvCountdownDays = findViewById(R.id.tvCountdownDays)
        tvTargetDate = findViewById(R.id.tvTargetDate)
        tvMastered = findViewById(R.id.tvMastered)
        tvCardTag = findViewById(R.id.tvCardTag)
        tvQuestion = findViewById(R.id.tvCardQuestion)
        tvAnswer = findViewById(R.id.tvCardAnswer)
        btnReveal = findViewById(R.id.btnReveal)
        btnMarkMastered = findViewById(R.id.btnMastered)
        progressBar = findViewById(R.id.progressBar)
    }

    // ------------------------------------------------------------ countdown

    private fun renderCountdown() {
        val interviewDate = LocalDate.of(
            Deck.INTERVIEW_YEAR, Deck.INTERVIEW_MONTH, Deck.INTERVIEW_DAY
        )
        val today = LocalDate.now()
        val daysLeft = ChronoUnit.DAYS.between(today, interviewDate)

        // A negative value means the date has already passed.
        tvCountdownDays.text = when {
            daysLeft > 0 -> daysLeft.toString()
            daysLeft == 0L -> "0"
            else -> "—"
        }

        tvTargetDate.text = interviewDate.format(
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
        )
    }

    // ----------------------------------------------------------------- card

    /**
     * Renders the current card. The answer starts hidden on every card change so
     * the deck is a genuine self-test rather than a reader.
     */
    private fun renderCard() {
        val card = Deck.cards[index]

        tvCardTag.text = card.tag
        tvQuestion.text = card.question

        tvAnswer.text = card.answer
        tvAnswer.visibility = if (answerVisible) View.VISIBLE else View.INVISIBLE

        btnReveal.text = getString(
            if (answerVisible) R.string.btn_reveal_hide else R.string.btn_reveal_show
        )

        renderMastered()
        renderProgress()
    }

    private fun renderMastered() {
        val isMastered = mastered.contains(index)
        btnMarkMastered.text = getString(
            if (isMastered) R.string.btn_marked else R.string.btn_mark_mastered
        )
        btnMarkMastered.setTextColor(
            if (isMastered) Color.parseColor("#C9A227") else Color.parseColor("#1E3A5F")
        )
    }

    private fun renderProgress() {
        tvMastered.text = "${mastered.size} / ${Deck.size}"
        progressBar.progress = if (Deck.size == 0) 0 else mastered.size * 100 / Deck.size
    }

    // -------------------------------------------------------------- actions

    private fun setupButtons() {
        findViewById<Button>(R.id.btnPrev).setOnClickListener { goTo(index - 1) }
        findViewById<Button>(R.id.btnNext).setOnClickListener { goTo(index + 1) }

        btnReveal.setOnClickListener {
            answerVisible = !answerVisible
            renderCard()
        }

        btnMarkMastered.setOnClickListener {
            if (!mastered.add(index)) {
                mastered.remove(index)
            }
            persistMastered()
            renderMastered()
            renderProgress()
        }
    }

    private fun setupSwipe() {
        val detector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false
                val dx = e2.x - e1.x
                val dy = e2.y - e1.y
                // Require a mostly-horizontal flick so vertical scrolling still works.
                if (kotlin.math.abs(dx) > SWIPE_THRESHOLD && kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                    goTo(if (dx < 0) index + 1 else index - 1)
                    return true
                }
                return false
            }
        })

        val cardArea = findViewById<View>(R.id.tvCardQuestion)
        cardArea.setOnTouchListener { v, event ->
            val handled = detector.onTouchEvent(event)
            if (event.actionMasked == MotionEvent.ACTION_UP) v.performClick()
            handled
        }
    }

    /** Moves through the deck, wrapping around at both ends. */
    private fun goTo(target: Int) {
        if (Deck.size == 0) return
        var next = target
        if (next < 0) next = Deck.size - 1
        if (next >= Deck.size) next = 0

        index = next
        answerVisible = false
        renderCard()
    }

    private fun persistMastered() {
        prefs.edit()
            .putStringSet(KEY_MASTERED, mastered.map { it.toString() }.toSet())
            .apply()
    }

    private companion object {
        const val KEY_MASTERED = "mastered_positions"
        const val SWIPE_THRESHOLD = 120f
    }
}
