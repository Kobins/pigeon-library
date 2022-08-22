package kr.lostwar.util.ui.text

enum class Backspaces(val mask: Int, val char: Char, val notPowerOf2: Boolean = false){
    BACKSPACE_1   (0b00000000001, '\uF801'),
    BACKSPACE_2   (0b00000000010, '\uF802'),
//    BACKSPACE_3   (0b00000000011, '\uF803', true),
    BACKSPACE_4   (0b00000000100, '\uF804'),
//    BACKSPACE_5   (0b00000000101, '\uF805', true),
//    BACKSPACE_6   (0b00000000110, '\uF806', true),
//    BACKSPACE_7   (0b00000000111, '\uF807', true),
    BACKSPACE_8   (0b00000001000, '\uF808'),
    BACKSPACE_16  (0b00000010000, '\uF809'),
    BACKSPACE_32  (0b00000100000, '\uF80A'),
    BACKSPACE_64  (0b00001000000, '\uF80B'),
    BACKSPACE_128 (0b00010000000, '\uF80C'),
    BACKSPACE_256 (0b00100000000, '\uF80D'),
    BACKSPACE_512 (0b01000000000, '\uF80E'),
    BACKSPACE_1024(0b10000000000, '\uF80F'),
    BACKSPACE_32768(0b1000000000000000, '\uF800'),
    ;

    override fun toString() = char.toString()

    companion object {
        @JvmStatic
        operator fun get(index: Int) = backspace[index]
        @JvmStatic
        private val backspace = (0..1024).toList().map {
            val builder = StringBuilder()
            for(space in values()){
                /*
                if(it == space.mask) {
                    builder.append(space.char)
                    break
                }
                if(space.notPowerOf2) continue
                 */
                val masked = (it and space.mask)
                if(masked != 0){
                    builder.append(space.char)
                }
            }
            builder.toString()
        }
    }
}