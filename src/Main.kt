import java.awt.*
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.security.SecureRandom
import javax.swing.*
import javax.swing.border.EmptyBorder

// --- ЦВЕТОВАЯ ПАЛИТРА (Темная тема) ---
object DarkTheme {
    val BACKGROUND = Color(43, 43, 43)       // Темный фон окна
    val PANEL_BG = Color(60, 63, 65)         // Фон панелей
    val TEXT_COLOR = Color(187, 187, 187)    // Светло-серый текст
    val ACCENT_COLOR = Color(75, 110, 175)   // Синий (фон кнопок)
    val ACCENT_HOVER = Color(90, 130, 200)   // Светлее при наведении
    val FIELD_BG = Color(69, 73, 74)         // Фон полей ввода
    val BORDER_COLOR = Color(100, 100, 100)  // Цвет рамок
}

class ModernPasswordGen : JFrame("Password Generator") {

    // Поле ввода длины
    private val lengthField = JTextField("12")

    // Галочки настроек
    private val checkUpper = true.createStyledCheckBox("A-Z (Заглавные)")
    private val checkLower = true.createStyledCheckBox("a-z (Строчные)")
    private val checkDigits = true.createStyledCheckBox("0-9 (Цифры)")
    private val checkSpecial = true.createStyledCheckBox("!@# (Спец. символы)")

    // Поле результата
    private val resultField = JTextField()

    // Кнопки (с черным текстом)
    private val generateButton = Color.BLACK.createStyledButton("СГЕНЕРИРОВАТЬ")
    private val copyButton = Color.BLACK.createStyledButton("КОПИРОВАТЬ")

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(500, 500)
        setLocationRelativeTo(null) // Центр экрана

        // Основная панель
        val mainPanel = JPanel()
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)
        mainPanel.background = DarkTheme.BACKGROUND
        mainPanel.border = EmptyBorder(30, 40, 30, 40) // Отступы

        // --- ЗАГОЛОВОК ---
        val titleLabel = JLabel("Генератор паролей")
        titleLabel.font = Font("Segoe UI", Font.BOLD, 24)
        titleLabel.foreground = Color.WHITE
        titleLabel.alignmentX = CENTER_ALIGNMENT

        // --- ПАНЕЛЬ ВЫБОРА ДЛИНЫ ---
        val lengthPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        lengthPanel.background = DarkTheme.BACKGROUND
        lengthPanel.maximumSize = Dimension(1000, 40)

        val lblText = JLabel("Длина пароля: ")
        lblText.foreground = DarkTheme.TEXT_COLOR
        lblText.font = Font("Segoe UI", Font.PLAIN, 14)

        // Настройка текстового поля
        lengthField.columns = 4
        lengthField.font = Font("Segoe UI", Font.BOLD, 14)
        lengthField.background = DarkTheme.FIELD_BG
        lengthField.foreground = Color.WHITE
        lengthField.caretColor = Color.WHITE // Белый курсор
        lengthField.horizontalAlignment = JTextField.CENTER
        lengthField.border = BorderFactory.createLineBorder(DarkTheme.BORDER_COLOR)

        lengthPanel.add(lblText)
        lengthPanel.add(lengthField)

        // --- ГРУППА ЧЕКБОКСОВ ---
        val optionsPanel = JPanel()
        optionsPanel.layout = GridLayout(4, 1, 5, 5)
        optionsPanel.background = DarkTheme.PANEL_BG
        optionsPanel.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DarkTheme.BORDER_COLOR, 1),
            EmptyBorder(10, 10, 10, 10)
        )
        optionsPanel.add(checkUpper)
        optionsPanel.add(checkLower)
        optionsPanel.add(checkDigits)
        optionsPanel.add(checkSpecial)
        optionsPanel.maximumSize = Dimension(1000, 150)

        // --- ПОЛЕ РЕЗУЛЬТАТА ---
        resultField.font = Font("Consolas", Font.BOLD, 18)
        resultField.background = DarkTheme.FIELD_BG
        resultField.foreground = Color(169, 183, 198) // Цвет кода
        resultField.horizontalAlignment = JTextField.CENTER
        resultField.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DarkTheme.ACCENT_COLOR, 1),
            EmptyBorder(10, 10, 10, 10)
        )
        resultField.isEditable = false
        resultField.maximumSize = Dimension(1000, 45)

        // --- СБОРКА ИНТЕРФЕЙСА ---
        mainPanel.add(titleLabel)
        mainPanel.add(Box.createRigidArea(Dimension(0, 20)))

        mainPanel.add(lengthPanel)
        mainPanel.add(Box.createRigidArea(Dimension(0, 10)))

        mainPanel.add(optionsPanel)
        mainPanel.add(Box.createRigidArea(Dimension(0, 20)))

        mainPanel.add(generateButton)
        mainPanel.add(Box.createRigidArea(Dimension(0, 10)))
        mainPanel.add(resultField)
        mainPanel.add(Box.createRigidArea(Dimension(0, 10)))
        mainPanel.add(copyButton)

        add(mainPanel)

        // --- ЛОГИКА КНОПКИ ГЕНЕРАЦИИ ---
        generateButton.addActionListener {
            try {
                val lengthText = lengthField.text.trim()
                val length = lengthText.toInt()

                if (length <= 0) {
                    JOptionPane.showMessageDialog(this, "Длина должна быть больше 0")
                    return@addActionListener
                }
                if (length > 999) {
                    JOptionPane.showMessageDialog(this, "Слишком длинный пароль!")
                    return@addActionListener
                }

                val password = generatePassword(
                    length,
                    checkUpper.isSelected,
                    checkLower.isSelected,
                    checkDigits.isSelected,
                    checkSpecial.isSelected
                )
                resultField.text = password
            } catch (_: NumberFormatException) {
                JOptionPane.showMessageDialog(this, "Введите число в поле длины", "Ошибка", JOptionPane.ERROR_MESSAGE)
            } catch (e: Exception) {
                JOptionPane.showMessageDialog(this, e.message, "Ошибка", JOptionPane.ERROR_MESSAGE)
            }
        }

        // --- ЛОГИКА КНОПКИ КОПИРОВАНИЯ (БЕЗ ОКНА) ---
        copyButton.addActionListener {
            if (resultField.text.isNotEmpty()) {
                val selection = StringSelection(resultField.text)
                Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)

                // Вместо всплывающего окна меняем текст кнопки
                val originalText = "КОПИРОВАТЬ"
                copyButton.text = "СКОПИРОВАНО!"

                // Таймер, который вернет текст обратно через 1.5 секунды (1500 мс)
                val timer = Timer(1500) {
                    copyButton.text = originalText
                }
                timer.isRepeats = false
                timer.start()
            }
        }
    }

    // Вспомогательные функции стилизации
    private fun Boolean.createStyledCheckBox(text: String): JCheckBox {
        val box = JCheckBox(text, this)
        box.background = DarkTheme.PANEL_BG
        box.foreground = DarkTheme.TEXT_COLOR
        box.font = Font("Segoe UI", Font.PLAIN, 14)
        box.isFocusPainted = false
        return box
    }

    private fun Color.createStyledButton(text: String): JButton {
        val btn = JButton(/* text = */ text)
        btn.font = Font("Segoe UI", Font.BOLD, 14)
        btn.background = DarkTheme.ACCENT_COLOR
        btn.foreground = this
        btn.isFocusPainted = false
        btn.border = EmptyBorder(10, 20, 10, 20)
        btn.alignmentX = CENTER_ALIGNMENT
        Dimension(1000, 45).also { btn.maximumSize = it }

        btn.addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent) { btn.background = DarkTheme.ACCENT_HOVER }
            override fun mouseExited(e: MouseEvent) { btn.background = DarkTheme.ACCENT_COLOR }
        })
        return btn
    }
}

fun main() {
    try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    } catch (_: Exception) {}

    SwingUtilities.invokeLater {
        ModernPasswordGen().isVisible = true
    }
}

// --- АЛГОРИТМ ГЕНЕРАЦИИ ---
fun generatePassword(length: Int, useUpper: Boolean, useLower: Boolean, useDigits: Boolean, useSpecial: Boolean): String {
    val charUpper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val charLower = "abcdefghijklmnopqrstuvwxyz"
    val charDigits = "0123456789"
    val charSpecial = "!@#$%^&*()_+-=[]{}|;:,.<>?"

    val allowedChars = StringBuilder()
    val password = StringBuilder()
    val random = SecureRandom()

    if (useUpper) { allowedChars.append(charUpper); password.append(charUpper[random.nextInt(charUpper.length)]) }
    if (useLower) { allowedChars.append(charLower); password.append(charLower[random.nextInt(charLower.length)]) }
    if (useDigits) { allowedChars.append(charDigits); password.append(charDigits[random.nextInt(charDigits.length)]) }
    if (useSpecial) { allowedChars.append(charSpecial); password.append(charSpecial[random.nextInt(charSpecial.length)]) }

    if (allowedChars.isEmpty()) throw IllegalArgumentException("Выберите хотя бы одну галочку!")

    val pool = allowedChars.toString()
    while (password.length < length) {
        password.append(pool[random.nextInt(pool.length)])
    }

    val passwordChars = password.toString().toCharArray()
    for (i in passwordChars.indices) {
        val randomIndex = random.nextInt(passwordChars.size)
        val temp = passwordChars[i]
        passwordChars[i] = passwordChars[randomIndex]
        passwordChars[randomIndex] = temp
    }

    return String(passwordChars).substring(0, length)
}