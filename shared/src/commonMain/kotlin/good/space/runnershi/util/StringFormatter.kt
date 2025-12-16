package good.space.runnershi.util

import kotlin.math.abs

/**
 * 문자열 포맷팅을 위한 정규식 패턴
 * 
 * 그룹 설명:
 * - 그룹 1: 플래그 (0, - 등)
 * - 그룹 2: 너비 (숫자)
 * - 그룹 3: 정밀도 전체 (.숫자)
 * - 그룹 4: 정밀도 숫자만
 * - 그룹 5: 타입 (d, f, s) 또는 이스케이프 문자 (%)
 * 
 * 성능 최적화: 컴파일 타임 상수로 선언하여 함수 호출마다 Regex 객체를 재생성하지 않도록 함
 */
private val FORMAT_PATTERN = Regex("%([0-]*)(\\d*)(\\.(\\d*))?([dfs%])")

/**
 * Kotlin Multiplatform 호환 문자열 포맷팅 유틸리티
 * 
 * Java의 String.format을 대체하는 순수 Kotlin 구현으로, KMP 환경에서 외부 라이브러리 없이 사용 가능합니다.
 * 
 * 지원 기능:
 * 
 * 타입 지정자:
 * - %s: 문자열 (String)
 * - %d: 정수 (Int, Long 등)
 * - %f: 실수 (Double, Float)
 * 
 * 플래그:
 * - -: 왼쪽 정렬 (예: %-5d -> "12   ")
 * - 0: 0 패딩 (예: %05d -> "00012")
 *   주의: 왼쪽 정렬(-)과 함께 사용 시 0 패딩은 무시됨
 * 
 * 너비 및 정밀도:
 * - %5d: 최소 너비 5 (오른쪽 정렬, 공백 패딩)
 * - %.2f: 소수점 이하 2자리
 * - %05.1f: 최소 너비 5, 소수점 이하 1자리, 0 패딩
 * 
 * 이스케이프:
 * - %%: 리터럴 % 문자 출력
 * 
 * 엣지 케이스 처리:
 * - 반올림 올림(Carry-over): 1.99를 %.1f로 포맷 시 2.0 출력
 * - 음수 0 패딩: %06.1f에 -1.2 입력 시 -001.2 출력 (부호 뒤에 0 채움)
 * - -0.0 처리: KMP 호환성을 위해 -0.0을 음수로 인식
 * - NaN/Infinity: NaN, Infinity, -Infinity 문자열로 출력
 * - Long 범위 초과: Long.MAX_VALUE를 초과하는 큰 숫자는 원본 문자열 반환
 * 
 * 사용 예시:
 * "Hello, %s!".format("World")  // "Hello, World!"
 * "Value: %05d".format(42)      // "Value: 00042"
 * "Price: %.2f".format(19.99)   // "Price: 19.99"
 * "%-5d%%".format(10)           // "10   %"
 * 
 * @param args 포맷 문자열의 플레이스홀더에 대응하는 인자들
 * @return 포맷팅된 문자열
 */
fun String.format(vararg args: Any): String {
    var argIndex = 0

    return FORMAT_PATTERN.replace(this) { matchResult ->
        val (flags, widthStr, _, precisionStr, type) = matchResult.destructured

        // %% 처리: 이스케이프 문자로 인식하여 리터럴 %로 변환 (인덱스 소모 안 함)
        if (type == "%") return@replace "%"

        if (argIndex >= args.size) return@replace matchResult.value
        val value = args[argIndex++]

        val width = widthStr.toIntOrNull() ?: 0
        val precision = precisionStr.toIntOrNull() ?: 6
        
        val isLeftAlign = flags.contains("-")
        val isZeroPad = flags.contains("0") && !isLeftAlign

        try {
            when (type) {
                "s" -> padString(value.toString(), width, ' ', isLeftAlign)
                "d" -> {
                    val longValue = (value as? Number)?.toLong() ?: return@replace matchResult.value
                    formatInt(longValue, width, isZeroPad, isLeftAlign)
                }
                "f" -> {
                    val doubleValue = (value as? Number)?.toDouble() ?: return@replace matchResult.value
                    formatDouble(doubleValue, width, precision, isZeroPad, isLeftAlign)
                }
                else -> matchResult.value
            }
        } catch (_: Exception) {
            matchResult.value
        }
    }
}

/**
 * Double 타입 포맷팅
 * 
 * 소수점 이하 자릿수를 지정하여 실수를 문자열로 변환합니다.
 * 반올림 시 자릿수 올림(Carry-over)을 처리하여 정확한 결과를 보장합니다.
 * 
 * @param value 포맷팅할 실수 값
 * @param width 최소 출력 너비 (0이면 무시)
 * @param decimalPlaces 소수점 이하 자릿수
 * @param zeroPad 0 패딩 사용 여부
 * @param isLeftAlign 왼쪽 정렬 여부
 * @return 포맷팅된 문자열
 */
private fun formatDouble(
    value: Double, 
    width: Int, 
    decimalPlaces: Int, 
    zeroPad: Boolean, 
    isLeftAlign: Boolean
): String {
    if (value.isNaN()) return padString("NaN", width, ' ', isLeftAlign)
    if (value.isInfinite()) {
        return padString(if (value > 0) "Infinity" else "-Infinity", width, ' ', isLeftAlign)
    }

    // Long 범위를 초과하는 큰 숫자는 toLong() 시 오버플로우가 발생하므로
    // 원본 문자열(지수 표기법 포함)을 그대로 반환
    if (value > Long.MAX_VALUE || value < Long.MIN_VALUE) {
        return padString(value.toString(), width, ' ', isLeftAlign)
    }

    val multiplier = getPowerOfTen(decimalPlaces)
    val isNegative = value < 0 || (value == 0.0 && 1.0 / value < 0)
    val absValue = abs(value)

    var integerPart = absValue.toLong()
    
    val fractionalPartRaw = (absValue - integerPart) * multiplier
    var fractionalPart = (fractionalPartRaw + 0.5).toLong()

    // 반올림 시 자릿수 올림 처리
    // 예: 1.99를 %.1f로 포맷 시 fractionalPart가 10이 되어 integerPart에 1을 더함
    if (fractionalPart >= multiplier.toLong()) {
        integerPart += 1
        fractionalPart = 0
    }

    val fractionalString = fractionalPart.toString().padStart(decimalPlaces, '0')
    val sign = if (isNegative) "-" else ""

    val rawResult = if (decimalPlaces > 0) {
        "$sign$integerPart.$fractionalString"
    } else {
        "$sign$integerPart"
    }

    // 0 패딩 처리: 음수일 경우 부호 뒤에 0을 채움
    // 예: %06.1f에 -1.2 입력 시 "-001.2" 출력
    if (zeroPad && width > rawResult.length) {
        return if (isNegative) {
            "-" + rawResult.substring(1).padStart(width - 1, '0')
        } else {
            rawResult.padStart(width, '0')
        }
    }

    return padString(rawResult, width, ' ', isLeftAlign)
}

/**
 * 정수 타입 포맷팅
 * 
 * @param value 포맷팅할 정수 값
 * @param width 최소 출력 너비 (0이면 무시)
 * @param zeroPad 0 패딩 사용 여부
 * @param isLeftAlign 왼쪽 정렬 여부
 * @return 포맷팅된 문자열
 */
private fun formatInt(value: Long, width: Int, zeroPad: Boolean, isLeftAlign: Boolean): String {
    val stringValue = value.toString()
    
    // 0 패딩 처리: 음수일 경우 부호 뒤에 0을 채움
    // 예: %05d에 -12 입력 시 "-0012" 출력
    if (zeroPad && width > stringValue.length) {
        return if (value < 0) {
            "-" + stringValue.substring(1).padStart(width - 1, '0')
        } else {
            stringValue.padStart(width, '0')
        }
    }
    
    return padString(stringValue, width, ' ', isLeftAlign)
}

/**
 * 문자열 패딩 처리 (왼쪽/오른쪽 정렬 지원)
 * 
 * @param str 패딩할 문자열
 * @param width 최소 너비
 * @param padChar 패딩에 사용할 문자
 * @param isLeftAlign true면 왼쪽 정렬(padEnd), false면 오른쪽 정렬(padStart)
 * @return 패딩된 문자열
 */
private fun padString(str: String, width: Int, padChar: Char, isLeftAlign: Boolean): String {
    if (str.length >= width) return str
    return if (isLeftAlign) str.padEnd(width, ' ') else str.padStart(width, padChar)
}

/**
 * 10의 거듭제곱 계산
 * 
 * 경량화된 구현으로 반복문을 사용하여 계산합니다.
 * 
 * @param exponent 지수
 * @return 10^exponent 값
 */
private fun getPowerOfTen(exponent: Int): Double {
    var result = 1.0
    repeat(exponent) { result *= 10.0 }
    return result
}
