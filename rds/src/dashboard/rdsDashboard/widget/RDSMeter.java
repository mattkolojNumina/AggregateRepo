package rdsDashboard.widget;

import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.geom.*;
import javax.swing.*;

import rdsDashboard.RDSDashboard;
import rdsDashboard.RDSWidget;


/**
 * A generic RDS dashboard widget that displays a graphical depiction of
 * an analog meter with a needle indicating the value.  Subclasses define
 * the needle position and displayed text.  
 */
public class RDSMeter
      extends RDSWidget {
   public static final int LABEL_HEIGHT = 20;

   protected static Color LO_COLOR = Color.RED;
   protected static Color HI_COLOR = Color.GREEN;
   protected static Color NORMAL_COLOR = Color.YELLOW;

   private static final double MIN_FRACTION = 0.0;
   private static final double MAX_FRACTION = 1.0;

   private JLabel meterLabel;       // label displayed under the meter
   private double meterFraction;    // 0.0..1.0
   private String meterText;        // text displayed on the meter
   private double loCutoff = 0.25;  // 0.0..1.0
   private double hiCutoff = 0.75;  // 0.0..1.0

   /**
    * Constructs an RDS Meter object.
    * 
    * @param   dashboard  the dashboard that contains this meter
    */
   public RDSMeter( RDSDashboard dashboard ) {
      super( dashboard );
      setName( "Generic RDS Meter" );

      createUI();

      setLabelText( getName() );
      setMeterFraction( 0.0 );
      setMeterText( "0" );
   }

   /**
    * Creates the user interface, consisting of a meter panel and a text
    * label.
    */
   private void createUI() {
      setLayout( new BorderLayout() );
      setBorder( BorderFactory.createEtchedBorder() );

      // center the meter within the widget's central panel
      JPanel meterContainerPanel = new JPanel( new FlowLayout(
            FlowLayout.CENTER, 0, 0 ) );
      meterContainerPanel.add( new MeterPanel() );
      add( meterContainerPanel, BorderLayout.CENTER );

      // add the label below the meter
      meterLabel = new JLabel( "", JLabel.CENTER );
      meterLabel.setPreferredSize( new Dimension( 0, LABEL_HEIGHT ) );
      add( meterLabel, BorderLayout.SOUTH );
   }

   /**
    * Determines the fill color for the meter; by default this is based
    * upon the value of the meter, with "low", "normal", and "high" regions,
    * separated by specific cutoffs.  Subclasses may override this behavior
    * to specify their own criteria for the fill color.
    * 
    * @return  the fill color
    */
   protected Color determineFillColor() {
      if (meterFraction <= loCutoff)
         return LO_COLOR;
      else if (meterFraction > hiCutoff)
         return HI_COLOR;
      return NORMAL_COLOR;
   }

   /**
    * Sets the text that is displayed in a label underneath the meter.
    * 
    * @param   labelText  the label text
    */
   public void setLabelText( String labelText ) {
      meterLabel.setText( labelText ); 
   }

   /**
    * Gets the current value for this meter, a value between 0.0 and 1.0,
    * inclusive.
    * 
    * @return  the current meter fraction
    */
   public double getMeterFraction() {
      return meterFraction;
   }

   /**
    * Sets the current value for this meter.  The value will be truncated
    * if it lies outside the range [0.0,1.0].
    * 
    * @param   meterFraction  the desired meter fraction
    */
   public void setMeterFraction( double meterFraction ) {
      if (meterFraction < MIN_FRACTION)
         this.meterFraction = MIN_FRACTION;
      else if (meterFraction > MAX_FRACTION)
         this.meterFraction = MAX_FRACTION;
      else
         this.meterFraction = meterFraction;
   }

   /**
    * Sets the text that is displayed on the face of the meter.  Typically,
    * this will be related (if not equal to) the value of the meter, but
    * there is no such requirement.
    * 
    * @param   text  the displayed text
    */
   public void setMeterText( String text ) {
      this.meterText = text;
   }

   /**
    * Sets the boundaries between the "low", "normal", and "high" regions
    * of the meter's value.  By default, the fill color of the meter
    * depends upon which region the meter is currently in.
    * 
    * @param   loCutoff  the cutoff between "low" and "normal"
    * @param   hiCutoff  the cutoff between "normal" and "high"
    */
   public void setCutoffs( double loCutoff, double hiCutoff ) {
      this.loCutoff = loCutoff;
      this.hiCutoff = hiCutoff;
   }

   /**
    * Inner class for displaying the actual meter.
    */
   class MeterPanel
         extends JPanel {
      private static final int PT_RADIUS = 3;  // radius of needle point
      private static final float TEXT_SIZE = 24.0f;

      private int meterHeight;
      private int meterWidth;

      protected int horizontalOffset;
      protected int verticalOffset;
      protected int radius;
      protected Point2D.Double center;

      protected int textX;
      protected int textY;

      /**
       * Constructs the meter panel.
       */
      private MeterPanel() {
         super( new BorderLayout() );
         calculateDimensions();
         setPreferredSize( new Dimension( meterWidth, meterHeight ) );
         setFont( getFont().deriveFont( TEXT_SIZE ) );
      }

      /**
       * Calculates various dimensions for the meter.  This method should
       * be called whenever the RDSMeter panel's border changes.
       */
      public void calculateDimensions() {
         Dimension parentSize = RDSMeter.this.getPreferredSize();
         Insets parentInsets = RDSMeter.this.getInsets();
         meterHeight = parentSize.height - LABEL_HEIGHT -
               parentInsets.top - parentInsets.bottom;
         meterWidth = Math.min( 2 * meterHeight,
               parentSize.width - parentInsets.left - parentInsets.right );

         Insets insets = getInsets();
         radius = Math.min( meterHeight - insets.top - insets.bottom,
               (meterWidth - insets.left - insets.right) / 2 ) -
               2 * PT_RADIUS;

         int centerX = (meterWidth - insets.left - insets.right ) / 2 +
               insets.left;
         int centerY = meterHeight - insets.bottom - PT_RADIUS;
         center = new Point2D.Double( centerX, centerY );

         horizontalOffset = centerX - radius;
         verticalOffset = centerY - radius;

         textX = centerX;
         textY = meterHeight * 2 / 3;
      }

      /**
       * Draws the meter on its panel.
       */
      public void paintComponent( Graphics g ) {
         super.paintComponent( g );

         Graphics2D g2d = (Graphics2D)g;

         // fill background based on meter fraction
         g2d.setPaint( determineFillColor() );
         double startArc = 180 * (1.0 - meterFraction);
         g2d.fill( new Arc2D.Double( horizontalOffset, verticalOffset,
               2 * radius, 2 * radius,
               startArc, 180 - startArc, Arc2D.PIE ) );

         // draw outline and needle
         g2d.setPaint( Color.BLACK );
         g2d.draw( new Arc2D.Double( horizontalOffset, verticalOffset,
               2 * radius, 2 * radius,
               0, 180, Arc2D.PIE ) );
         Point2D.Double p = determineNeedlePoint();
         g2d.draw( new Line2D.Double( center.x, center.y, p.x, p.y ) );
         g2d.fill( new Ellipse2D.Double( p.x - PT_RADIUS, p.y - PT_RADIUS,
               2 * PT_RADIUS, 2 * PT_RADIUS ) );

         // draw meter text, centered
         TextLayout meterTextLayout = new TextLayout( meterText,
               g2d.getFont(), g2d.getFontRenderContext() );
         int meterTextWidth = (int) meterTextLayout.getBounds().getWidth();
         g2d.drawString( meterText, textX - meterTextWidth / 2, textY );
      }

      /**
       * Gets the location of the point of the needle, based on the current
       * meter fraction.
       * 
       * @return  the needle point
       */
      private Point2D.Double determineNeedlePoint() {
         Point2D.Double p = new Point2D.Double();
         double angle = Math.PI * (1.0 - meterFraction);
         p.x = horizontalOffset + radius + radius * Math.cos( angle );
         p.y = verticalOffset + radius - radius * Math.sin( angle );
         return p;
      }
   }  /* end MeterPanel inner class */

}  /* end RDSMeter class */
