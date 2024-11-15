/*
 * RDSPageNavigator.java
 * 
 * (c) 2007, Numina Group, Inc.
 */

package rdsDashboard;

import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.*;

import rds.RDSUtil;


/**
 * A utility for navigating through pages of results.  Typical usage would
 * involve a table populated via an SQL query that returns a large number
 * of results.
 * <p>
 * A component that wishes to register with this navigator must implement
 * the {@code PageNavigable} interface to ensure the presence of callback
 * functions that are called when navigation occurs. In addition, it is
 * the responsibility of the navigable component to keep this navigator
 * updated when the total number of results changes.
 */
public class RDSPageNavigator
      implements ActionListener {
   private static final int GAP = 5;
   private static final int DEFAULT_RESULTS_PER_PAGE = 100;
   private static final Dimension BUTTON_SIZE = new Dimension( 25, 20 );

   // images used for navigation arrows
   private static final ImageIcon firstIcon = RDSUtil.createImageIcon(
         RDSPageNavigator.class, "images/moveFirstPg.gif" );
   private static final ImageIcon prevIcon = RDSUtil.createImageIcon(
         RDSPageNavigator.class, "images/movePrev.gif" );
   private static final ImageIcon nextIcon = RDSUtil.createImageIcon(
         RDSPageNavigator.class, "images/moveNext.gif" );
   private static final ImageIcon lastIcon = RDSUtil.createImageIcon(
         RDSPageNavigator.class, "images/moveLastPg.gif" );

   // the component for which this object is navigating
   private PageNavigable navigableComponent;

   // navigation variables
   private int resultsPerPage;
   private int totalResults;
   private int currentPage;

   // ui variables
   private JPanel navPanel;
   private JButton firstButton, prevButton, nextButton, lastButton;
   private JTextField currentPageField;
   private JLabel resultsDisplayLabel, totalPageLabel;

   /**
    * An interface for components that utilize page navigation.
    */
   public interface PageNavigable {

      /**
       * Invoked when the page navigation changes.
       */
      public void pageNavigationUpdated();
   }


   /*
    * --- constructors ---
    */

   /**
    * Constructs a page navigator with a default number of results per page.
    */
   public RDSPageNavigator( PageNavigable navigableComponent ) {
      this( navigableComponent, DEFAULT_RESULTS_PER_PAGE );
   }

   /**
    * Constructs a page navigator with the specified number of results
    * displayed per page.
    * 
    * @param   resultsPerPage  the number of results per page
    */
   public RDSPageNavigator( PageNavigable navigableComponent,
         int resultsPerPage ) {
      this.navigableComponent = navigableComponent;
      this.resultsPerPage = (resultsPerPage > 0) ?
            resultsPerPage : DEFAULT_RESULTS_PER_PAGE;
      this.currentPage = 1;
   }


   /*
    * --- member access methods ---
    */

   /**
    * Gets the current page.
    */
   public int getCurrentPage() {
      return currentPage;
   }

   /**
    * Gets the number of results displayed per page.
    */
   public int getResultsPerPage() {
      return resultsPerPage;
   }

   /**
    * Sets the number of results displayed per page.  In addition, navigation
    * resets to the first page of results.
    */
   public void setResultsPerPage( int resultsPerPage ) {
      this.resultsPerPage = resultsPerPage;
      goToFirst();
   }

   /**
    * Sets the total number of results.  Navigation is set to remain at the
    * current page, but an update still occurs.
    */
   public void setTotalResults( int totalResults ) {
      this.totalResults = totalResults;
      goToPage( currentPage );
   }


   /*
    * --- navigation methods ---
    */

   /**
    * Calculates the page number of the last page of results.
    */
   private int getLastPage() {
      return (totalResults - 1) / resultsPerPage + 1;
   }

   /**
    * Navigates to the specified page.  If the specified page is less than
    * one, the first page is selected; if the specified page is greater
    * than the total number of pages, the last page is selected.
    * <p>
    * This method notifies the registered navigable component that a
    * page navigation update has occurred by executing the {@code
    * pageNavigationUpdated} method required by the {@code PageNavigable}
    * interface.
    * 
    * @param  page  the page to which to navigate
    */
   public void goToPage( int page ) {
      currentPage = Math.max( Math.min( page, getLastPage() ), 1 );
      updateNavigationPanel();

      navigableComponent.pageNavigationUpdated();
   }

   /**
    * Navigates to the next page of results.
    */
   public void goToNext() {
      goToPage( currentPage + 1 );
   }

   /**
    * Navigates to the previous page of results.
    */
   public void goToPrev() {
      goToPage( currentPage - 1 );
   }

   /**
    * Navigates to the first page of results.
    */
   public void goToFirst() {
      goToPage( 1 );
   }

   /**
    * Navigates to the last page of results.
    */
   public void goToLast() {
      goToPage( getLastPage() );
   }

   /**
    * Modifies the result-generating query to limit results to those that
    * will be shown on the current page.
    * 
    * @param   query  the original query for generating results
    * @return  the modified query
    */
   public String getNavigationQuery( String query ) {
      int skip = (currentPage - 1) * resultsPerPage;
      String navQuery = query + " LIMIT " + skip + ", " + resultsPerPage;
      return navQuery;
   }


   /*
    * --- ui methods ---
    */

   /**
    * Creates a panel to display page navigation information and controls,
    * including a message that indicates which results are currently
    * displayed.
    */
   public void createNavigationPanel() {
      createNavigationPanel( true );
   }

   /**
    * Creates a panel to display page navigation information and controls.
    * Optionally, a message label may be included that indicates which
    * results are currently displayed.
    * 
    * @param   showResultsMessage  {@code true} if the results display
    *          message should be shown, {@code false} otherwise
    */
   public void createNavigationPanel( boolean showResultsMessage ) {
      resultsDisplayLabel = new JLabel();

      firstButton = new JButton( firstIcon );
      firstButton.setPreferredSize( BUTTON_SIZE );
      firstButton.setActionCommand( "First" );
      firstButton.addActionListener( this );

      prevButton = new JButton( prevIcon );
      prevButton.setPreferredSize( BUTTON_SIZE );
      prevButton.setActionCommand( "Previous" );
      prevButton.addActionListener( this );

      nextButton = new JButton( nextIcon );
      nextButton.setPreferredSize( BUTTON_SIZE );
      nextButton.setActionCommand( "Next" );
      nextButton.addActionListener( this );

      lastButton = new JButton ( lastIcon );
      lastButton.setPreferredSize( BUTTON_SIZE );
      lastButton.setActionCommand( "Last" );
      lastButton.addActionListener( this );

      // create and layout the "Page x of y" subpanel
      currentPageField = new JTextField( 2 );
      currentPageField.setHorizontalAlignment( SwingConstants.CENTER );
      currentPageField.setMaximumSize( currentPageField.getPreferredSize() );
      currentPageField.setActionCommand( "Current" );
      currentPageField.addActionListener( this );

      totalPageLabel = new JLabel();
      JPanel pagePanel = new JPanel();
      pagePanel.setLayout( new BoxLayout( pagePanel, BoxLayout.X_AXIS ) );
      pagePanel.add( new JLabel( "Page ", JLabel.RIGHT ) );
      pagePanel.add( currentPageField );
      pagePanel.add( totalPageLabel );

      // assemble the navigation panel
      navPanel = new JPanel();
      navPanel.setLayout( new BoxLayout( navPanel, BoxLayout.X_AXIS ) );

      if (showResultsMessage) {
         navPanel.add( resultsDisplayLabel );
         navPanel.add( Box.createHorizontalGlue() );
      }

      navPanel.add( firstButton );
      navPanel.add( prevButton );
      navPanel.add( Box.createHorizontalStrut( GAP ) );
      navPanel.add( pagePanel );
      navPanel.add( Box.createHorizontalStrut( GAP ) );
      navPanel.add( nextButton );
      navPanel.add( lastButton );

      updateNavigationPanel();
   }

   /**
    * Gets the panel that contains navigation information and controls.
    * 
    * @return  the navigation panel object
    */
   public JPanel getNavigationPanel() {
      if (navPanel == null)
         createNavigationPanel();

      return navPanel;
   }

   /**
    * Updates the text and ui elements in the navigation panel.
    */
   private void updateNavigationPanel() {
      if (navPanel == null)
         return;

      resultsDisplayLabel.setText( getResultsDisplayMessage() );

      boolean isNotFirst = (currentPage > 1);
      boolean isNotLast  = (currentPage < getLastPage());
      firstButton.setEnabled( isNotFirst );
      prevButton.setEnabled( isNotFirst );
      nextButton.setEnabled( isNotLast );
      lastButton.setEnabled( isNotLast );

      currentPageField.setText( Integer.toString( currentPage ) );
      totalPageLabel.setText( " of " + getLastPage() );
   }

   /**
    * Obtains from this navigator a {@code String} that announces the
    * records currently being viewed.
    * 
    * @return  the display {@code String}
    */
   public String getResultsDisplayMessage() {
      if (totalResults < 1)
         return "No results to display";

      int first = (currentPage - 1) * resultsPerPage + 1;
      int last = Math.min( first + resultsPerPage - 1, totalResults );
      return "Displaying results " + first + " through " + last +
            " of " + totalResults;
   }


   /*
    * --- ActionListener method ---
    */

   /**
    * Handles actions performed on UI elements within this panel.
    */
   @Override
   public void actionPerformed( ActionEvent evt ) {
      String action = evt.getActionCommand();
      if (action == null)
         return;

      if (action.equals( "First" ))
         goToFirst();
      else if (action.equals( "Previous" ))
         goToPrev();
      else if (action.equals( "Next" ))
         goToNext();
      else if (action.equals( "Last" ))
         goToLast();
      else if (action.equals( "Current" )) {
         int newCurrentPage = currentPage;
         try {
            newCurrentPage = Integer.valueOf( currentPageField.getText() );
         } catch (NumberFormatException ex) {}
         goToPage( newCurrentPage );
      }
   }

}  // end RDSPageNavigator class
