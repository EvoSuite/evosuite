/*
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/*****************************************************************
   F O L D   B U T T O N   P L U G - I N
*****************************************************************

Version       1.00
Date          30 August 2011
Author        Peter Fox
Contact       author at vulpeculox.net
Web page      vulpeculox.net/bits/js/dvnt/foldButton
Description   This jQuery plugin allows show/hide buttons to be
              easily attached to one or more sections of a document.
              This can be as simple as $('div#someDiv').foldButton();
              in the ready() function.
Documentation See documentation.htm               
Licence       Public domain              
*****************************************************************/

(function( $ ){
  $.fn.foldButton = function(Options,Argument) {

  var fbHEADINGS = 'H1,H2,H3,H4,H5,H6';  
  
  var defaults = {
      'closedText'       : 'Open',
      'openedText'       : 'Close',
      'compact'          : true,
      'cascadeClose'     : true,
      'cascadeOpen'      : false,
      'open'             : false,
      'title'            : '',    
      'titleLength'      : 100,
      'speed'            : 300,
      'radioGroup'       : ''      
    };

  var bu = '<span class=foldButton>?</span>';
    
  // Peculiar way of setting up sub-routines inside the single namespace
  // This is as clear as I can make it. 
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  
  // ----------------------------------------
  // Set class and text according to 'open' data value
  // ----------------------------------------
  if(Options=='setDisplay'){
    return this.each(function(ix) {
      var b = $(this);
      var isOpen = b.data('open');
      if(isOpen===true){
        b.removeClass('fbClosed');         // toggle display class
        b.addClass('fbOpen');              // toggle display class
        b.text(b.data('openedText'));     // set correct display text
        b.attr('title','');
      }else{
        b.removeClass('fbOpen');
        b.addClass('fbClosed');
        b.text(b.data('closedText'));     // this is now closed
        b.attr('title',b.data('title'));   // full title
      }      
    });  
  }
  
  // ----------------------------------------
  // Close command  
  // Only supply visible (:visible) arguments!
  // ----------------------------------------
  if(Options=='close'){
    return this.each(function(ix) {
      var bu = $(this);
      var isOpen = bu.data('open');
      if(isOpen==true){bu.click();}      
    });  
  }

  // ----------------------------------------
  // Open command.
  // Only supply visible (:visible) arguments!
  // ----------------------------------------
  if(Options=='open'){
    return this.each(function(ix) {
      var bu = $(this);
      var isOpen = bu.data('open');
      if(isOpen==false){bu.click();}      
    });  
  }

  // ----------------------------------------
  // Radio close.
  // Close all buttons with the radio group 
  // ----------------------------------------
  if(Options=='radioCloseRest'){
    return this.each(function(ix) {
      var bu = $(this);
      var rg = bu.data('radioGroup');
      if(rg){
        var rparent = bu.parent().parent();  // might be parent or parent's parent.  this is 'safe'
        rparent.find('.foldButton:visible').foldButton('radioCloseThis',rg);
      }  
    });  
  }

  // ----------------------------------------
  // Radio close.
  // Close button if radio group matches argument
  // ----------------------------------------
  if(Options=='radioCloseThis'){
    return this.each(function(ix) {
      var bu = $(this);
      var rg = bu.data('radioGroup');
      if(rg==Argument){
        if(bu.data('open')==true){bu.click();}      
      }  
    });  
  }

  
  // ----------------------------------------
  // Initialise
  // Options is not any of the preceeding
  // ----------------------------------------
  var settings = defaults;
  if(Options){$.extend(settings,Options);}
  
  return this.each(function(ix) {
      var host     = $(this);
      var tag      = host.get(0).tagName;
      var isH      = (fbHEADINGS.indexOf(tag)>-1);
      var target   = (isH) ? host.next() : host;   
      var buttonInner = null;
      
      // Create a title from target content
      var title = settings['title'];
      if(title ==''){
        var title = target.attr('title');
        if(! title){ 
        var titleLength = settings['titleLength'];
        if(titleLength<0){titleLength=0;}
        if(titleLength>1000){titleLength=1000;}
        title = target.text().substr(0,titleLength)+'...';}
      }
      
      // various placing of 1 or 2 buttons
      if(isH){
        host.prepend(bu);
        var button = host.children('span.foldButton:first');
      }else{
        if(settings['compact']==true){                     // inner button for close
          host.prepend(bu);                             
          buttonInner = host.children('span.foldButton:first');
        }
        host.before(bu);                                   // outer button for open/close
        var button = host.prev('span.foldButton');
      }
      
      // store data with button(s)
      for(k in settings){
        button.data(k,settings[k]);
        if(buttonInner){
          buttonInner.data(k,settings[k]);
        }
      }
      if(title){button.data('title',title)};      
      var ct = button.data('closedText');
      if(ct.indexOf('TITLE')>-1){
        ct = ct.replace(/TITLE/,title.substr(0,100));
        button.data('closedText',ct);
      }  
      
      
      // open/close target
      if(button.data('open')===true){
        target.show();
      }else{
        target.hide();
      }  
      // May have to force double-button open states
      if(buttonInner){
        if(button.data('open')===true){button.hide();}    
        button.data('open',false);                     // 2-button outer   always displays "open"
        buttonInner.data('open',true);                 // 2-button inner   always displays "close"
      }
      // implement the button displays
      button.foldButton('setDisplay');       
      if(buttonInner){buttonInner.foldButton('setDisplay');}
      
      // inner DIV button click (must be close)
      // --------------------------------------
      if(buttonInner){
        buttonInner.click(function(){
          var bu = $(this);
          var parent = bu.parent();
          var buttonOpen = parent.prev();
          parent.hide(bu.data('speed'));
          buttonOpen.show();
          
          // possibly cascade
          if(bu.data('cascadeClose')===true){                                  // possibly cascade
            parent.children().children('span.foldButton:visible').foldButton('close');
          }
          return false;
          
        });
      }
  
      // 1-button toggle or 2-button open
      // --------------------------------
      button.click(function(){
        var bu = $(this);
        var currentlyOpen = bu.data('open');
        var parent = bu.parent();
        var parentTag = parent.get(0).tagName;
        var parentIsH = (fbHEADINGS.indexOf(parentTag)>-1);
        var isCompact = bu.data('compact');
        
        if((isCompact==true)&&(isH==false)){  
          // TWO BUTTONS must be opening  no need to hack display
          bu.hide();                 // hide
          if(bu.data('radioGroup')){
            // The RADIO feature means that all other foldButtons with the SAME PARENT
            // will be closed so only this one is open
            bu.foldButton('radioCloseRest');
          }
          target.show(bu.data('speed'));          // show target  
        }else{
          // ONE BUTTON
          if(currentlyOpen){                                                   // CLOSING
            bu.data('open',false);             // flip state
            target.hide(bu.data('speed'));
            if(bu.data('cascadeClose')===true){                                // possibly cascade
              target.children().children('span.foldButton:visible').foldButton('close');
            }  
          }else{                                                               // OPENING 
            if(bu.data('radioGroup')){
              // The RADIO feature means that all other foldButtons with the SAME PARENT
              // will be closed so only this one is open
              bu.foldButton('radioCloseRest');
            }
            bu.data('open',true);             // flip state
            target.show(bu.data('speed'));
            if(bu.data('cascadeOpen')===true){                                 // possibly cascade
              target.children().children('span.foldButton').foldButton('open');
            }  
            
          }
          bu.foldButton('setDisplay');
        }
        return false;
      });  
      
      
    });
  };
})( jQuery );