package kr.lostwar.util.ui.text

enum class Spaces(val mask: Int, val char: Char){
    SPACE_1   (0b00000000001, '\uF821'),
    SPACE_2   (0b00000000010, '\uF822'),
    SPACE_4   (0b00000000100, '\uF824'),
    SPACE_8   (0b00000001000, '\uF828'),
    SPACE_16  (0b00000010000, '\uF829'),
    SPACE_32  (0b00000100000, '\uF82A'),
    SPACE_64  (0b00001000000, '\uF82B'),
    SPACE_128 (0b00010000000, '\uF82C'),
    SPACE_256 (0b00100000000, '\uF82D'),
    SPACE_512 (0b01000000000, '\uF82E'),
    SPACE_1024(0b10000000000, '\uF82F');

    override fun toString() = char.toString()

    companion object {
        @JvmStatic
        operator fun get(index: Int) = space[index]
        @JvmStatic
        private val space = (0..1024).toList().map {
            val builder = StringBuilder()
            for(space in values()){
                val masked = (it and space.mask)
                if(masked != 0){
                    builder.append(space.char)
                }
            }
            builder.toString()
        }
    }
}