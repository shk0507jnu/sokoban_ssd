package sokoban.ui;

import java.awt.event.KeyEvent;
import java.util.Map;

/**
 * 한글 입력기가 켜져 있어도 단축키(Z, Y, R, V, WASD 등)가 먹도록 키 이벤트를 보정한다.
 * 한글 모드에서는 keyCode가 VK_UNDEFINED/VK_PROCESSKEY로 오거나 keyChar가 자모(ㅛ 등)로 오는 경우가 있어,
 * 두벌식 자판 배열 기준으로 물리 키(영문)에 대응시킨다.
 */
final class KeyNormalizer {
    /** Windows VK_PROCESSKEY (입력기가 키를 처리 중). */
    static final int VK_PROCESSKEY = 0xE5;

    private KeyNormalizer() {
    }

    private static final Map<Character, Integer> JAMO = Map.ofEntries(
            Map.entry('ㅂ', KeyEvent.VK_Q), Map.entry('ㅈ', KeyEvent.VK_W), Map.entry('ㄷ', KeyEvent.VK_E),
            Map.entry('ㄱ', KeyEvent.VK_R), Map.entry('ㅅ', KeyEvent.VK_T), Map.entry('ㅛ', KeyEvent.VK_Y),
            Map.entry('ㅕ', KeyEvent.VK_U), Map.entry('ㅑ', KeyEvent.VK_I), Map.entry('ㅐ', KeyEvent.VK_O),
            Map.entry('ㅔ', KeyEvent.VK_P), Map.entry('ㅁ', KeyEvent.VK_A), Map.entry('ㄴ', KeyEvent.VK_S),
            Map.entry('ㅇ', KeyEvent.VK_D), Map.entry('ㄹ', KeyEvent.VK_F), Map.entry('ㅎ', KeyEvent.VK_G),
            Map.entry('ㅗ', KeyEvent.VK_H), Map.entry('ㅓ', KeyEvent.VK_J), Map.entry('ㅏ', KeyEvent.VK_K),
            Map.entry('ㅣ', KeyEvent.VK_L), Map.entry('ㅋ', KeyEvent.VK_Z), Map.entry('ㅌ', KeyEvent.VK_X),
            Map.entry('ㅊ', KeyEvent.VK_C), Map.entry('ㅍ', KeyEvent.VK_V), Map.entry('ㅠ', KeyEvent.VK_B),
            Map.entry('ㅜ', KeyEvent.VK_N), Map.entry('ㅡ', KeyEvent.VK_M),
            // 쌍자음/이중모음 (Shift + 키)
            Map.entry('ㅃ', KeyEvent.VK_Q), Map.entry('ㅉ', KeyEvent.VK_W), Map.entry('ㄸ', KeyEvent.VK_E),
            Map.entry('ㄲ', KeyEvent.VK_R), Map.entry('ㅆ', KeyEvent.VK_T), Map.entry('ㅒ', KeyEvent.VK_O),
            Map.entry('ㅖ', KeyEvent.VK_P));

    /** 물리 키 코드를 알아낼 수 있으면 보정한 코드, 아니면 원래 코드. */
    static int keyCodeOf(int keyCode, char keyChar, int extendedKeyCode) {
        if (keyCode != KeyEvent.VK_UNDEFINED && keyCode != VK_PROCESSKEY) return keyCode;
        Integer vk = JAMO.get(keyChar);
        if (vk == null && (extendedKeyCode & 0x01000000) != 0) {
            vk = JAMO.get((char) (extendedKeyCode & 0x00FFFFFF));
        }
        return vk != null ? vk : keyCode;
    }

    static KeyEvent normalize(KeyEvent e) {
        int code = keyCodeOf(e.getKeyCode(), e.getKeyChar(), e.getExtendedKeyCode());
        if (code == e.getKeyCode()) return e;
        return new KeyEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiersEx(), code, e.getKeyChar());
    }
}
