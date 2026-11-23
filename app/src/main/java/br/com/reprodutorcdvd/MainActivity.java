package br.com.reprodutorcdvd;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setStatusBarColor(Color.rgb(9, 13, 34));
        window.setNavigationBarColor(Color.rgb(8, 11, 26));
        setContentView(new DiscPlayerView());
    }

    private class DiscPlayerView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private final float density;
        private int selectedTab = 0;
        private boolean driveConnected = false;
        private boolean isPlaying = false;
        private String source = "Nenhuma mídia inserida";

        private final int backgroundTop = Color.rgb(8, 15, 49);
        private final int backgroundBottom = Color.rgb(28, 12, 55);
        private final int panel = Color.rgb(20, 27, 62);
        private final int panelLight = Color.rgb(31, 39, 82);
        private final int royalBlue = Color.rgb(60, 96, 255);
        private final int purple = Color.rgb(145, 82, 255);
        private final int textPrimary = Color.rgb(245, 247, 255);
        private final int textSecondary = Color.rgb(171, 181, 215);
        private final int textMuted = Color.rgb(112, 125, 170);
        private final int success = Color.rgb(75, 224, 166);

        DiscPlayerView() {
            super(MainActivity.this);
            density = getResources().getDisplayMetrics().density;
            setFocusable(true);
            setFocusableInTouchMode(true);
        }

        private float d(float value) {
            return value * density;
        }

        private void fill(Canvas canvas, int color) {
            paint.setShader(null);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color);
        }

        private void rounded(Canvas canvas, float left, float top, float right, float bottom,
                             float radius, int color) {
            fill(canvas, color);
            canvas.drawRoundRect(new RectF(d(left), d(top), d(right), d(bottom)),
                    d(radius), d(radius), paint);
        }

        private void text(Canvas canvas, String value, float x, float y, float size,
                          int color, boolean bold) {
            paint.setShader(null);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color);
            paint.setTextSize(d(size));
            paint.setTypeface(Typeface.create("sans", bold ? Typeface.BOLD : Typeface.NORMAL));
            canvas.drawText(value, d(x), d(y), paint);
        }

        private void centeredText(Canvas canvas, String value, float centerX, float y,
                                  float size, int color, boolean bold) {
            paint.setTextSize(d(size));
            paint.setTypeface(Typeface.create("sans", bold ? Typeface.BOLD : Typeface.NORMAL));
            text(canvas, value, centerX - (paint.measureText(value) / density / 2f), y, size,
                    color, bold);
        }

        private void gradientRect(Canvas canvas, float left, float top, float right, float bottom,
                                  float radius, int start, int end) {
            paint.setStyle(Paint.Style.FILL);
            paint.setShader(new LinearGradient(d(left), d(top), d(right), d(bottom),
                    start, end, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(new RectF(d(left), d(top), d(right), d(bottom)),
                    d(radius), d(radius), paint);
            paint.setShader(null);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float width = getWidth() / density;
            float height = getHeight() / density;

            paint.setShader(new LinearGradient(0, 0, 0, getHeight(), backgroundTop,
                    backgroundBottom, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
            paint.setShader(null);

            drawGlow(canvas, width * 0.92f, 72, 170, royalBlue, 0.12f);
            drawGlow(canvas, width * 0.08f, height - 116, 190, purple, 0.10f);

            drawHeader(canvas, width);
            float contentTop = 88;
            if (selectedTab == 0) {
                drawHome(canvas, width, contentTop);
            } else if (selectedTab == 1) {
                drawLibrary(canvas, width, contentTop);
            } else {
                drawSettings(canvas, width, contentTop);
            }
            drawBottomBar(canvas, width, height);
        }

        private void drawGlow(Canvas canvas, float cx, float cy, float radius, int color, float alpha) {
            paint.setShader(new android.graphics.RadialGradient(d(cx), d(cy), d(radius),
                    Color.argb((int) (255 * alpha), Color.red(color), Color.green(color), Color.blue(color)),
                    Color.TRANSPARENT, Shader.TileMode.CLAMP));
            canvas.drawCircle(d(cx), d(cy), d(radius), paint);
            paint.setShader(null);
        }

        private void drawHeader(Canvas canvas, float width) {
            // Marca
            fill(canvas, royalBlue);
            canvas.drawCircle(d(28), d(35), d(18), paint);
            fill(canvas, Color.rgb(18, 25, 63));
            canvas.drawCircle(d(28), d(35), d(7), paint);
            fill(canvas, Color.WHITE);
            canvas.drawCircle(d(33), d(29), d(2), paint);

            text(canvas, "REPRODUTOR", 55, 31, 14, textPrimary, true);
            text(canvas, "CD  •  DVD", 55, 49, 11, textSecondary, false);

            rounded(canvas, width - 152, 20, width - 20, 55, 16, Color.argb(70, 255, 255, 255));
            fill(canvas, driveConnected ? success : textMuted);
            canvas.drawCircle(d(width - 137), d(37.5f), d(4), paint);
            text(canvas, driveConnected ? "DRIVE CONECTADO" : "AGUARDANDO DRIVE",
                    width - 126, 42, 10, textPrimary, true);
        }

        private void drawHome(Canvas canvas, float width, float top) {
            text(canvas, "Olá, pronto para reproduzir?", 24, top + 16, 24, textPrimary, true);
            text(canvas, "Sua mídia física, com uma experiência moderna.", 24, top + 43, 13, textSecondary, false);

            float cardTop = top + 67;
            float cardBottom = cardTop + 258;
            rounded(canvas, 20, cardTop, width - 20, cardBottom, 24, panel);

            gradientRect(canvas, 34, cardTop + 16, 166, cardBottom - 16, 18,
                    Color.rgb(36, 67, 170), Color.rgb(93, 42, 166));
            drawDiscArt(canvas, 100, cardTop + 104, 62);
            centeredText(canvas, driveConnected ? "MÍDIA PRONTA" : "MODO DEMO", 100,
                    cardBottom - 36, 10, Color.argb(215, 255, 255, 255), true);

            text(canvas, driveConnected ? "Drive USB detectado" : "Nenhuma mídia inserida",
                    190, cardTop + 52, 21, textPrimary, true);
            text(canvas, driveConnected ? "Insira um CD ou DVD para começar" :
                    "Conecte um drive USB e insira um CD ou DVD", 190, cardTop + 80, 12,
                    textSecondary, false);

            drawStatusPill(canvas, 190, cardTop + 103, driveConnected ? "PRONTO PARA LER" : "AGUARDANDO CONEXÃO",
                    driveConnected ? success : textMuted);

            drawMiniControl(canvas, width - 175, cardTop + 155, "◀", false);
            drawPlayButton(canvas, width - 124, cardTop + 180, driveConnected && isPlaying);
            drawMiniControl(canvas, width - 73, cardTop + 155, "▶", false);

            rounded(canvas, 34, cardBottom + 20, width - 34, cardBottom + 70, 15,
                    Color.argb(55, 255, 255, 255));
            text(canvas, "Fonte atual", 50, cardBottom + 42, 11, textSecondary, false);
            text(canvas, source, 50, cardBottom + 61, 13, textPrimary, true);
            text(canvas, driveConnected ? "●" : "○", width - 64, cardBottom + 51, 22,
                    driveConnected ? success : textMuted, true);

            float quickTop = cardBottom + 102;
            text(canvas, "Acesso rápido", 24, quickTop, 16, textPrimary, true);
            drawQuickCard(canvas, 20, quickTop + 16, (width - 56) / 2f, "Conectar drive",
                    "USB OTG", "↯", royalBlue);
            drawQuickCard(canvas, 36 + (width - 56) / 2f, quickTop + 16,
                    (width - 56) / 2f, "Abrir arquivo", "ISO / VIDEO_TS", "▣", purple);
        }

        private void drawLibrary(Canvas canvas, float width, float top) {
            text(canvas, "Biblioteca", 24, top + 16, 24, textPrimary, true);
            text(canvas, "Seus discos e reproduções recentes", 24, top + 43, 13, textSecondary, false);

            drawLibraryCard(canvas, 20, top + 76, width - 20, "Nenhum disco salvo",
                    "Quando você inserir uma mídia, ela aparecerá aqui.", royalBlue, "＋");
            drawLibraryCard(canvas, 20, top + 152, width - 20, "Histórico de reprodução",
                    "Seus CDs e DVDs recentes ficam organizados nesta área.", purple, "◷");

            rounded(canvas, 20, top + 254, width - 20, top + 338, 20, panel);
            text(canvas, "Compatibilidade", 38, top + 284, 15, textPrimary, true);
            text(canvas, "CD • CD-R/RW • DVD • DVD-R/RW", 38, top + 309, 12, textSecondary, false);
            text(canvas, "Em breve: menus DVD e capítulos", 38, top + 330, 11, textMuted, false);
        }

        private void drawSettings(Canvas canvas, float width, float top) {
            text(canvas, "Configurações", 24, top + 16, 24, textPrimary, true);
            text(canvas, "Ajuste o comportamento do seu reprodutor", 24, top + 43, 13, textSecondary, false);

            drawSettingRow(canvas, 20, top + 78, width - 20, "Reprodução automática",
                    "Iniciar a mídia assim que o disco for detectado", true);
            drawSettingRow(canvas, 20, top + 146, width - 20, "Buscar metadados online",
                    "Consultar informações do CD pela internet", true);
            drawSettingRow(canvas, 20, top + 214, width - 20, "Navegação por controle remoto",
                    "Setas, OK, voltar e Menu", false);

            rounded(canvas, 20, top + 310, width - 20, top + 368, 18, panel);
            text(canvas, "Versão do protótipo", 38, top + 338, 12, textSecondary, false);
            text(canvas, "0.1.0  •  interface inicial", 38, top + 358, 13, textPrimary, true);
        }

        private void drawStatusPill(Canvas canvas, float x, float y, String label, int color) {
            float pillWidth = label.length() > 15 ? 138 : 113;
            rounded(canvas, x, y - 17, x + pillWidth, y + 7, 12, Color.argb(55, Color.red(color),
                    Color.green(color), Color.blue(color)));
            fill(canvas, color);
            canvas.drawCircle(d(x + 12), d(y - 5), d(3), paint);
            text(canvas, label, x + 21, y, 9, color, true);
        }

        private void drawDiscArt(Canvas canvas, float cx, float cy, float radius) {
            paint.setShader(new LinearGradient(d(cx - radius), d(cy - radius), d(cx + radius),
                    d(cy + radius), Color.rgb(10, 17, 47), Color.rgb(44, 24, 91), Shader.TileMode.CLAMP));
            canvas.drawCircle(d(cx), d(cy), d(radius), paint);
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(d(2));
            paint.setColor(Color.argb(80, 255, 255, 255));
            for (int i = 1; i < 5; i++) {
                canvas.drawCircle(d(cx), d(cy), d(radius * i / 5f), paint);
            }
            paint.setStyle(Paint.Style.FILL);
            fill(canvas, Color.argb(170, 255, 255, 255));
            canvas.drawCircle(d(cx), d(cy), d(8), paint);
            fill(canvas, Color.rgb(56, 82, 198));
            canvas.drawCircle(d(cx), d(cy), d(3), paint);
        }

        private void drawPlayButton(Canvas canvas, float cx, float cy, boolean playing) {
            paint.setShadowLayer(d(12), 0, d(4), Color.argb(80, 80, 82, 255));
            setLayerType(View.LAYER_TYPE_SOFTWARE, paint);
            paint.setShader(new LinearGradient(d(cx - 26), d(cy - 26), d(cx + 26), d(cy + 26),
                    royalBlue, purple, Shader.TileMode.CLAMP));
            canvas.drawCircle(d(cx), d(cy), d(27), paint);
            paint.clearShadowLayer();
            paint.setShader(null);
            fill(canvas, Color.WHITE);
            if (playing) {
                canvas.drawRoundRect(new RectF(d(cx - 8), d(cy - 9), d(cx - 2), d(cy + 9)), d(2), d(2), paint);
                canvas.drawRoundRect(new RectF(d(cx + 2), d(cy - 9), d(cx + 8), d(cy + 9)), d(2), d(2), paint);
            } else {
                path.reset();
                path.moveTo(d(cx - 6), d(cy - 11));
                path.lineTo(d(cx + 10), d(cy));
                path.lineTo(d(cx - 6), d(cy + 11));
                path.close();
                canvas.drawPath(path, paint);
            }
        }

        private void drawMiniControl(Canvas canvas, float cx, float cy, String symbol, boolean active) {
            fill(canvas, active ? Color.WHITE : textMuted);
            paint.setTextSize(d(18));
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            canvas.drawText(symbol, d(cx - 7), d(cy + 6), paint);
        }

        private void drawQuickCard(Canvas canvas, float left, float top, float cardWidth,
                                   String title, String subtitle, String icon, int accent) {
            rounded(canvas, left, top, left + cardWidth, top + 78, 18, panelLight);
            fill(canvas, Color.argb(45, Color.red(accent), Color.green(accent), Color.blue(accent)));
            canvas.drawCircle(d(left + 30), d(top + 37), d(18), paint);
            centeredText(canvas, icon, left + 30, top + 43, 20, accent, true);
            text(canvas, title, left + 58, top + 32, 13, textPrimary, true);
            text(canvas, subtitle, left + 58, top + 53, 10, textSecondary, false);
        }

        private void drawLibraryCard(Canvas canvas, float left, float top, float right,
                                     String title, String subtitle, int accent, String icon) {
            rounded(canvas, left, top, right, top + 60, 17, panel);
            fill(canvas, Color.argb(45, Color.red(accent), Color.green(accent), Color.blue(accent)));
            canvas.drawCircle(d(left + 31), d(top + 30), d(17), paint);
            centeredText(canvas, icon, left + 31, top + 37, 18, accent, true);
            text(canvas, title, left + 59, top + 26, 13, textPrimary, true);
            text(canvas, subtitle, left + 59, top + 45, 10, textSecondary, false);
        }

        private void drawSettingRow(Canvas canvas, float left, float top, float right,
                                    String title, String subtitle, boolean enabled) {
            rounded(canvas, left, top, right, top + 56, 16, panel);
            text(canvas, title, left + 18, top + 23, 13, textPrimary, true);
            text(canvas, subtitle, left + 18, top + 42, 10, textSecondary, false);
            rounded(canvas, right - 52, top + 18, right - 18, top + 38, 11,
                    enabled ? Color.rgb(62, 83, 180) : Color.rgb(54, 60, 89));
            fill(canvas, enabled ? Color.rgb(141, 107, 255) : textMuted);
            canvas.drawCircle(d(enabled ? right - 29 : right - 41), d(top + 28), d(7), paint);
        }

        private void drawBottomBar(Canvas canvas, float width, float height) {
            float top = height - 78;
            paint.setShader(new LinearGradient(0, d(top), 0, getHeight(),
                    Color.argb(0, 9, 13, 34), Color.argb(245, 9, 13, 34), Shader.TileMode.CLAMP));
            canvas.drawRect(0, d(top - 18), getWidth(), getHeight(), paint);
            paint.setShader(null);

            String[] labels = {"Início", "Biblioteca", "Ajustes"};
            String[] icons = {"⌂", "▤", "⚙"};
            float slot = width / 3f;
            for (int i = 0; i < 3; i++) {
                float cx = slot * i + slot / 2f;
                int color = selectedTab == i ? textPrimary : textMuted;
                if (selectedTab == i) {
                    fill(canvas, Color.argb(40, 104, 114, 255));
                    canvas.drawRoundRect(new RectF(d(cx - 35), d(top + 2), d(cx + 35), d(top + 37)),
                            d(18), d(18), paint);
                    color = Color.rgb(165, 149, 255);
                }
                centeredText(canvas, icons[i], cx, top + 25, 19, color, true);
                centeredText(canvas, labels[i], cx, top + 51, 10, color, selectedTab == i);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() != MotionEvent.ACTION_UP) {
                return true;
            }

            float x = event.getX() / density;
            float y = event.getY() / density;
            float width = getWidth() / density;
            float height = getHeight() / density;

            if (y >= height - 92) {
                selectedTab = Math.max(0, Math.min(2, (int) (x / (width / 3f))));
                invalidate();
                return true;
            }

            if (selectedTab == 0) {
                if (y >= 150 && y <= 275 && x >= width - 175 && x <= width - 70) {
                    if (driveConnected) {
                        isPlaying = !isPlaying;
                        source = isPlaying ? "Reprodução em andamento" : "Reprodução pausada";
                    } else {
                        Toast.makeText(MainActivity.this, "Conecte um drive USB para reproduzir", Toast.LENGTH_SHORT).show();
                    }
                    invalidate();
                    return true;
                }

                if (y >= 400 && y <= 520 && x <= width / 2f + 18) {
                    openUsbPlayer();
                    return true;
                }
            }
            return true;
        }

        private void openUsbPlayer() {
            try {
                Intent intent = new Intent();
                intent.setClassName(MainActivity.this, "dev.discmount.app.MainActivity");
                MainActivity.this.startActivity(intent);
            } catch (RuntimeException error) {
                Toast.makeText(MainActivity.this,
                        "O módulo de reprodução ainda não está disponível: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public boolean dispatchKeyEvent(android.view.KeyEvent event) {
            if (event.getAction() == android.view.KeyEvent.ACTION_UP) {
                int key = event.getKeyCode();
                if (key == android.view.KeyEvent.KEYCODE_DPAD_LEFT && selectedTab > 0) {
                    selectedTab--;
                    invalidate();
                    return true;
                }
                if (key == android.view.KeyEvent.KEYCODE_DPAD_RIGHT && selectedTab < 2) {
                    selectedTab++;
                    invalidate();
                    return true;
                }
                if (key == android.view.KeyEvent.KEYCODE_DPAD_CENTER || key == android.view.KeyEvent.KEYCODE_ENTER) {
                    if (selectedTab == 0 && driveConnected) {
                        isPlaying = !isPlaying;
                        source = isPlaying ? "Reprodução em andamento" : "Reprodução pausada";
                        invalidate();
                    }
                    return true;
                }
            }
            return super.dispatchKeyEvent(event);
        }
    }
}
