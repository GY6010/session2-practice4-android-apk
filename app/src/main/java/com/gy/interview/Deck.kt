package com.gy.interview

/**
 * The flashcard deck.
 *
 * The content is the candidate's own interview preparation: the questions a
 * French engineering-school panel is expected to ask during the UT Group
 * registration interview, paired with short answers that can be spoken aloud.
 *
 * Keeping the data in Kotlin rather than in a database is deliberate: the deck
 * is small, static, and version-controlled, which makes it trivially reviewable
 * in a pull request and requires no schema migration.
 */
data class Card(
    val tag: String,
    val question: String,
    val answer: String
)

object Deck {

    /** The interview date used for the countdown. Adjust when it is confirmed. */
    const val INTERVIEW_YEAR = 2026
    const val INTERVIEW_MONTH = 12   // 1-based
    const val INTERVIEW_DAY = 15

    val cards: List<Card> = listOf(

        Card(
            tag = "SCHOOL",
            question = "Which school do you prefer?",
            answer = "I lean towards UTC. Its strong focus on system engineering aligns " +
                "with my Electronic Information background. However, I would be truly " +
                "honoured to be accepted by either school."
        ),

        Card(
            tag = "SCHOOL",
            question = "Why UTC and not UTT?",
            answer = "Two reasons. UTC's system engineering focus matches my project " +
                "experience — on the Siemens Cup line I had to connect hardware, PLC and " +
                "HMI into one working system. Second, project-based training suits how I " +
                "learn: I understand principles by building and debugging."
        ),

        Card(
            tag = "SCHOOL",
            question = "What is the difference between UTT and UTC?",
            answer = "Both are top-tier engineering schools within the UT group. UTC is " +
                "known for comprehensive engineering and system engineering, while UTT has " +
                "distinct advantages in digital systems. Both are highly relevant to my " +
                "major in Electronic Information."
        ),

        Card(
            tag = "SCHOOL",
            question = "Would you accept UTT if you are assigned there?",
            answer = "Yes, absolutely. Both schools share the same UT engineering training " +
                "model, and UTT's digital systems fit my major well. I would be glad to go " +
                "there."
        ),

        Card(
            tag = "RESEARCH",
            question = "Which research direction interests you most?",
            answer = "My project experience is in automation and system integration — PLC, " +
                "HMI and communication. That is the direction I want to continue in."
        ),

        Card(
            tag = "PROGRAMME",
            question = "What do you know about UTT's programme?",
            answer = "I know UTT has strengths in digital systems and ICT, which match my " +
                "major. I would be glad to learn more about its programme."
        ),

        Card(
            tag = "MOTIVATION",
            question = "You are a first-year master's student. Why apply for an " +
                "engineering degree now?",
            answer = "My current programme is mainly coursework-based. What I want is " +
                "systematic engineering training with real projects and company experience " +
                "— that is the French engineering model."
        ),

        Card(
            tag = "LANGUAGE",
            question = "How is your English and French?",
            answer = "CET-6, and I can read English technical documentation on my own. My " +
                "French is at the beginner stage; I plan to start before enrolment."
        ),

        Card(
            tag = "PROJECT",
            question = "Tell me about the Siemens Cup project.",
            answer = "We built an unmanned production line on S7-1200 PLCs over PROFINET, " +
                "linking six stations into one automated material flow. I developed and " +
                "commissioned the WinCC HMI — I/O and tag planning, PLC signal acquisition, " +
                "live line status, and tiered fault alarms. We won 2nd Prize in 2024."
        ),

        Card(
            tag = "PROJECT",
            question = "What was the hardest part of that project?",
            answer = "Isolating signal-chain faults. A symptom on the HMI could originate in " +
                "the hardware wiring, the PROFINET network, or the HMI configuration. I " +
                "debugged in three layers to narrow it down instead of guessing."
        ),

        Card(
            tag = "BACKUP",
            question = "Sorry, could you repeat the question?",
            answer = "Use this when you did not catch the question. Do not guess — ask. " +
                "A calm request to repeat reads as attentive, not as weak English."
        ),

        Card(
            tag = "BACKUP",
            question = "Sorry, I am not very familiar with that. Could I answer it from " +
                "what I have done?",
            answer = "The escape line for a question you cannot answer. Then pivot to your " +
                "Siemens Cup project and stop talking after one or two sentences."
        )
    )

    val size: Int get() = cards.size
}
