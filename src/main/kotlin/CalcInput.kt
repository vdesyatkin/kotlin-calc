import java.io.InputStream
import java.io.OutputStream

data class CalcInput(val args: List<Double> = emptyList(),
                     val stringArgs: List<String> = emptyList(),
                     val state: Any? = null,
                     val inputStream: InputStream? = null,
                     val outputStream: OutputStream? = null)