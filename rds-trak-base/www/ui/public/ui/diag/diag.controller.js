(function()
{
  angular
    .module('ui')
      .controller('DiagController',diagController);

  angular
    .module('ui')
      .config(diagConfig);

  diagController.$inject = ['$scope','$http','$interval','$routeParams',
                            'Global','DbFactory'];

  function
  diagController($scope,$http,$interval,$routeParams,Global,DbFactory)
  {
    var periodic;

    var canvas;
    var stage;
    var container;
    $scope.area = "";
    var hint = {};

    var objects = [];
    var lookup = {};
    
    // Tweaks
    var hintOffsetX = 0;
    var hintOffsetY = 0;
    var hintPad = 5;
    var hintTextTypeface = 'Arial';
    var hintTextColor = '#000';
    var hintTextSize = 20;
    var hintBackgroundColor = 'yellow';
    var textTypefaceDefault = 'Arial';
    var textColorDefault = '#000';
    var textSizeDefault = '30'; //it's a string deal with it
    var responsiveVertical = true; //set false to always fit entire width
    
    
    var set = { nominalWidth: 1024,
                nominalHeight: 768,
                fudge: 10,
                zoom: {value: 1,
                       min: {enabled: false,
                             value:   1},
                       max: {enabled: false,
                             value:   1} },
                last: {x: -1,
                       y: -1 } };

    // Config

    var enableZoom = false;
    var enablePan = false;

    // // // // //

    function
    mousewheel(evt)
    {
      var fraction = 0.10;

      var delta = evt.wheelDelta;
      if(delta>0)
        set.zoom.value *= 1 + fraction;
      if(delta<0)
        set.zoom.value *= 1 - fraction;

      if(   (set.zoom.min.enabled)
         && (set.zoom.value < set.zoom.min.value))
        set.zoom.value = set.zoom.min.value;
      if(   (set.zoom.max.enabled)
         && (set.zoom.value > set.zoom.max.value))
        set.zoom.value = set.zoom.max.value;

      container.scaleX = set.zoom.value * stage.canvas.width / set.nominalWidth;
      container.scaleY = set.zoom.value * stage.canvas.width / set.nominalWidth;
      stage.update();
    }

    function
    mousemove(evt)
    {
      if(evt.buttons==0){
        set.last.x = -1;
        set.last.y = -1;
      } else if((evt.buttons&1)==1){
        if((set.last.x>0)&&(set.last.y>0)){
          container.x += evt.clientX-set.last.x;
          container.y += evt.clientY-set.last.y;
          stage.update();
        }
        set.last.x = evt.clientX;
        set.last.y = evt.clientY;
      }
    }

    function
    mouseOver(event)
    {
      var o = event.target.index;
      if(o>0){
        if(objects[o].hint){
          hint.text.set({text: objects[o].hint || '', x: hint.pad, y:hint.pad});
          var bounds = hint.text.getBounds();
          var w = bounds.width  + 2 * hint.pad;
          var h = bounds.height + 2 * hint.pad;
          hint.shape.graphics
            .clear()
            .beginFill(hintBackgroundColor)
            .drawRoundRect(0,0,w,h,hint.pad);
          
          var minLeft = Math.max( (objects[o].item.x - w) , 0 );
          var maxLeft = Math.max( Math.min( (set.nominalWidth - w), objects[o].item.x ),0);

          //var x = objects[o].item.x + 2*hint.pad + hintOffsetX;
          var x = (minLeft+maxLeft)/2;
          var y = objects[o].item.y - 2*hint.pad - h + hintOffsetY;
          hint.container.x = x;
          hint.container.y = y;
          hint.container.set({visible: true});
        }
      } else {
        hint.container.set({visible: false});
      }
    }

    function
    easelResize()
    {
      var freeWidth = $("#body").width();
      var bar = $("#headBar").height();
      var freeHeight = $(window).height() - bar - set.fudge;

      var availableAspectRatio = freeWidth/freeHeight;
      var nativeAspectRatio = set.nominalWidth/set.nominalHeight;

      if(availableAspectRatio>nativeAspectRatio && responsiveVertical){
        stage.canvas.height = freeHeight;
        var scaled = freeHeight / set.nominalHeight;
        stage.canvas.width = set.nominalWidth * scaled;
      } else {
        stage.canvas.width = freeWidth;
        var scaled = freeWidth / set.nominalWidth;
        stage.canvas.height = set.nominalHeight * freeWidth / set.nominalWidth;
      }
      container.scaleX = scaled;
      container.scaleY = scaled;

      set.zoom.value = 1;
      container.x = 0;
      container.y = 0;

      stage.update();
    }

    function
    easelTick(e)
    {
//      stage.update();
    }

    function
    easelInit()
    {
      canvas = document.getElementById('diag');

      if(enablePan){
        canvas.addEventListener("mousemove",mousemove);
      }
      if(enableZoom){
        canvas.addEventListener("mousewheel",mousewheel);
        canvas.addEventListener("DOMMouseScroll",mousewheel);
      }

      stage  = new createjs.Stage(canvas);
      stage.enableMouseOver();

      container = new createjs.Container();
      stage.addChild(container);


      createjs.Ticker.setFPS(10);
      createjs.Ticker.addEventListener("tick",easelTick);

      window.addEventListener('resize',easelResize,false);

//      easelResize();
    }

    function
    loadComplete()
    {
      for(var o=0; o < objects.length; o++){
        if(objects[o].type=='zone' ||
           objects[o].type=='estop' ||
           objects[o].type=='fault' ||
           objects[o].type=='jam' ||
           objects[o].type=='linefull' ||
           objects[o].type=='motor' ||
           objects[o].type=='statictext' ||
           objects[o].type=='text'){

          if(objects[o].type!='text' && objects[o].type!='statictext'){
            objects[o].item = new createjs.Shape();
          } else {
            objects[o].item = new createjs.Text(
              objects[o].hint,
              Number(objects[o].size||textSizeDefault)+'px '+(objects[o].font||textTypefaceDefault),
              (objects[o].color||textColorDefault));
            objects[o].item.set({textAlign: 'center'});
            if(objects[o].type=='text'){
              if(objects[o].justification=='0.0')
                objects[o].item.set({textAlign: 'left'});
              if(objects[o].justification=='1.0')
                objects[o].item.set({textAlign: 'right'});
            }
            if(objects[o].type=='statictext'){
              objects[o].item.text = objects[o].text;
              objects[o].item.set({textAlign: 'left'});
            }
          }

          objects[o].item.name = objects[o].name;
          objects[o].item.x = Number(objects[o].x);
          objects[o].item.y = Number(objects[o].y);
          objects[o].item.rotation = Number(objects[o].rotation);
          container.addChild(objects[o].item);
        }

        else if(objects[o].type=='image'){}

        else {
          console.error('Unsupported object '+objects[o].name+' of type '+objects[o].type);
          console.log(objects[o]);
          continue;
        }

        objects[o].item.index = o;
        objects[o].item.on("mouseover",mouseOver);
      }

      hint.pad = hintPad;
      hint.textSize = hintTextSize;
      hint.container = new createjs.Container();
      hint.shape = new createjs.Shape();
      hint.shape.graphics.beginFill(hintBackgroundColor);
      hint.shape.graphics.drawRoundRect(0,0,100,100,hint.pad);
      hint.container.addChild(hint.shape);
      hint.text = new createjs.Text("hint",hint.textSize+"px "+hintTextTypeface,hintTextColor);
      hint.container.addChild(hint.text);
      hint.container.set({visible: false});
      container.addChild(hint.container);

      stage.update();
      refresh();

      periodic = $interval(refresh,2000);
    }

    function
    loadSuccess(file)
    {
      var current = 0;
      var params;
      var reType = /.*\[(.*)\].*/;
      var reProp = /(.*)=(.*)/;

      var lines = file.split('\n');

      objects = [];
      for(var l=0; l<lines.length; l++){
        switch(lines[l].charAt(0)){
          case '#':
          case ' ':
            break;
          case '[':
            params = reType.exec(lines[l]);
            current = objects.push({}) - 1;
            objects[current]['type'] = params[1];
            break;
          default:
            if(params = reProp.exec(lines[l]))
              objects[current][params[1]]=params[2];
            break;
        }
      }

      lookup = {};
      for(var o=0; o<objects.length; o++){
        lookup[objects[o].name] = o;
        if(objects[o].type=='image'){
          objects[o].item = new createjs.Bitmap('images/diagnostic/'+objects[o].file);
          objects[o].item.image.onload = loadComplete;
          container.addChild(objects[o].item);
          set.nominalWidth  = Number(objects[o].width);
          set.nominalHeight = Number(objects[o].height);
          easelResize();
        }
      }
    }

    function
    loadInit(filename)
    {
      $http.get(filename)
        .success(loadSuccess)
        .error(function(err){console.error(err);});
    }


    function
    setup()
    {
      easelInit();
      easelResize();
      loadInit($scope.rds);
      stage.update();
    }

    function
    update(web)
    {
      for(var w=0; w<web.length; w++){
        if( ($scope.area !="") && ($scope.area != web[w].area) )
          continue;
        var o = lookup[web[w].name];
        if(o){
          objects[o].item.visible = (web[w].value != '');
          objects[o].hint         = web[w].hint;

          switch(objects[o].type){
            case 'zone':
              var colorZone = '#fff';
              if(web[w].value=='box'){
                colorZone = '#c85';
              } else if(web[w].value=='fault'){
                colorZone = '#e22';
              } else if(web[w].value!=''){
                colorZone = '#e72';
                objects[o].hint = 'Error: zone object '+objects[o].name+
                                  ' has unknown value "'+web[w].value+'"';
              }
              objects[o].item
                .graphics
                .beginFill(colorZone)
                .drawRect(0,0,
                          Number(objects[o].width),
                          Number(objects[o].height) );
              break;
            case 'estop':
              var colorEstop = '#e22';
              if(web[w].value && web[w].value!='fault'){
                colorEstop = '#e72';
                objects[o].hint = 'Error: estop object '+objects[o].name+
                                  ' has unknown value "'+web[w].value+'"';
              }
              objects[o].item
                .graphics
                .beginFill(colorEstop)
                .drawRect(0,0,
                          Number(objects[o].width),
                          Number(objects[o].height) );
              break;
            case 'fault':
              var colorFault = '#e22';
              if(web[w].value && web[w].value!='fault'){
                colorFault = '#e72';
                objects[o].hint = 'Error: fault object '+objects[o].name+
                                  ' has unknown value "'+web[w].value+'"';
              }
              objects[o].item
                .graphics
                .beginFill(colorFault)
                .drawRect(0,0,
                          Number(objects[o].width),
                          Number(objects[o].height) );
              break;
            case 'jam':
              var colorJam = '#e27';
              if(web[w].value && web[w].value!='fault'){
                colorJam = '#e72';
                objects[o].hint = 'Error: jam object '+objects[o].name+
                                  ' has unknown value "'+web[w].value+'"';
              }
              objects[o].item
                .graphics
                .beginFill(colorJam)
                .drawEllipse(0,0,
                             Number(objects[o].width),
                             Number(objects[o].height) );
              break;
            case 'linefull':
              var colorLinefull = '#ee2';
              if(web[w].value && web[w].value!='full'){
                colorLinefull = '#e72';
                objects[o].hint = 'Error: linefull object '+objects[o].name+
                                  ' has unknown value "'+web[w].value+'"';
              }
              objects[o].item
                .graphics
                .beginFill(colorLinefull)
                .drawRect(0,0,
                          Number(objects[o].width),
                          Number(objects[o].height) );
              break;
            case 'motor':
              var colorMotor = '#fff';
              if(web[w].value=='run'){
                colorMotor = '#2c2';
              } else if(web[w].value=='fault'){
                colorMotor = '#e22';
              } else if(web[w].value!=''){
                colorMotor = '#e72';
                objects[o].hint = 'Error: motor object '+objects[o].name+
                                  ' has unknown value "'+web[w].value+'"';
              }
              objects[o].item
                .graphics
                .beginFill(colorMotor)
                .drawEllipse(0,0,
                             Number(objects[o].width),
                             Number(objects[o].height) );
              break;
            case 'text':
              objects[o].item.text = web[w].value;
              break;
            case 'statictext':
              objects[o].item.text = objects[o].text ;
            default:
          }
        }
      }
      stage.update();
    }

    function
    refresh()
    {
      DbFactory.post({topic: 'webObjects',
                      action: 'all'})
        .success(update)
        .error(function(err){console.error(err);});
    }

    function
    spaceTitle(unspaced)
    {
      var chars = unspaced.split('');
      var i = 0;
      do {
        if(/[A-Z]|[0-9]/.test(chars[i])){
          chars.splice(i,0,' ');
          i++;
        }
        i++;
      } while(chars[i]);
      Global.setTitle('Diagnostics: '+chars.join(''));
    }

    function
    init()
    {
      Global.setTitle('Diagnostics: No File Selected');
      Global.recv('refresh',refresh,$scope);
      if($routeParams.area){
         $scope.area = $routeParams.area;
      }
      if($routeParams.rds){
        spaceTitle($routeParams.rds);
        $scope.rds = "images/diagnostic/" + $routeParams.rds + ".rds";
        setup();
      }
    }

    $scope.$on('$destroy',function(){
      $interval.cancel(periodic);
    });

    init();
  }

  function
  diagConfig($routeProvider)
  {
    $routeProvider
      .when('/diag',{controller: 'DiagController',
                     templateUrl: '/ui/diag/diag.view.html'});
  }

}())
