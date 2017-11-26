package io.gitub.ticklemonster.robots;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;

public class CreateTexture {
  // there is no way to instatiate this directly
  // only the static methods

  public static Texture FromBitmapFont( BitmapFont font, String text ) {
    //Gdx.app.log("SpriteString::TextureFromBFString","started");

    GlyphLayout layout = new GlyphLayout( font, text );
    GlyphLayout.GlyphRun glyphrun = layout.runs.get(0);
    Pixmap pixmap = new Pixmap(
      MathUtils.ceil(glyphrun.width) + glyphrun.glyphs.size + 1,
      MathUtils.ceil(layout.height),
      Pixmap.Format.RGBA4444  //RGB888
    );
    //pixmap.setColor(new Color(0.37f, 0.37f, 0.37f, 1));
    //pixmap.fill();
    pixmap.setBlending(Pixmap.Blending.SourceOver);
    pixmap.setColor(Color.BLACK);

    int offset = 0;
    Pixmap fontpage = null;
    int pageloaded = -1;
    for( int i=0; i<glyphrun.glyphs.size; i++ ) {
      BitmapFont.Glyph glyph = glyphrun.glyphs.get(i);
      offset += MathUtils.ceil(glyphrun.xAdvances.get(i));

      if( fontpage == null || pageloaded != glyph.page ) {
        fontpage = new Pixmap(Gdx.files.internal(font.getData().getImagePath(glyph.page)));
        pageloaded = glyph.page;
      }

      pixmap.drawPixmap(fontpage,
        glyph.srcX, glyph.srcY, glyph.width+1, glyph.height+1,
        offset, 0,
        MathUtils.ceil(glyph.width * font.getScaleX()),
        MathUtils.ceil(glyph.height * font.getScaleY())
      );
    }

    //Gdx.app.log("SpriteString::TextureFromBFString","finished");
    Texture rval = new Texture(pixmap);
    if( fontpage != null ) fontpage.dispose();
    pixmap.dispose();

    return rval;
  }


}
