import java.awt.*;

import javax.swing.*;

public class ImageArea extends JPanel
{
   /**
    *  Displayed image's Image object.
    */

   private Image image;
   private Image background;

   /**
    *  Construct an ImageArea by setting its default preferred size to
    *  (300, 300).
    */

   public ImageArea ()
   {
      // Set the ImageArea's default preferred size to 300 pixels by 300
      // pixels.

      setPreferredSize (new Dimension (300, 300));
   }

   /**
    *  Repaint the ImageArea with the current image's pixels.
    */
   public void paintComponent (Graphics g)
   {
      // Repaint the component's background.
      super.paintComponent (g);

      // If an image has been defined, draw that image using the Component
      // layer of this ImageArea object as the ImageObserver.

      //269 x 49
      //System.out.println("paint component...");
      if (image != null)
          g.drawImage (image, 0, 0, this);
      else if(background != null)
    	  g.drawImage (background, ((this.getWidth() / 2) - 135), ((this.getHeight() / 2) - 25), this);
   }

   /**
    *  Establish a new image and update the display.
    *
    *  @param image new image's Image reference
    */

   public void setImage (Image image)
   {
	  // Save the image for later repaint.

      this.image = image;

      // Set this panel's preferred size to the image's size, to influence the
      // display of scrollbars.

      setPreferredSize (new Dimension (image.getWidth (this),
                                       image.getHeight (this)));

      // Invalidate this panel component, walk up the containment hierarchy to
      // the first validateRoot (the JScrollPane), and validate that root --
      // which also validates the ImageArea component. Validation results in
      // the JScrollPane displaying scrollbars or not -- and the image being
      // displayed.

      revalidate ();    
      repaint(); //necessário pq trabalhando com imagens grandes sem repaint() não funciona.
   }
   
   public void setImageBackground(Image image)
   {
	   this.background = image;
   }
   
   public void removeImage()
   {
	   this.image = null;
	   setPreferredSize (new Dimension (100, 100));
	   revalidate();
   }
}
