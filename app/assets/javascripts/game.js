var Bar = function(paper, x, y, vals) {
  var barAnimateTime = 2000;
  var barHeight = 20;

  var colors = ["#0f0", "#00f", "#f00"];

  var rect = function(x, y, w, h, color) {
    return paper.rect(x,y,w,h).attr({fill: color, stroke: "none"});
  };

  var bars = {};

  for(var i in vals) {
    var b = vals[i];
    var size = b.size || 0;
    b.bar = rect(x,y,size, barHeight, colors[i % colors.length]);
    bars[b.name] = b;
  }

  var resizeBar = function(bar, size) {
    bar.animate({width: size}, barAnimateTime);
  };

  var destroy = function() {
    for(var i in bars) {
      if (bars.hasOwnProperty(i)) {
        bars[i].bar.stop();
        bars[i] = null;
      }
    }
  };

  return {
    setSize: function(name, n) {
      resizeBar(bars[name].bar, n);
    },
    vals: vals,
    destroy: destroy
  };
};

var Bars = function(bars) {
  var index = {};

  for(var i in bars) {
    var bar = bars[i];
    var vals = bar.vals;
    for (var j in vals) {
      var val = vals[j];
      index[val.name] = bar;
    };
  };

  var destroy = function() {
    for(var i in bars) {
      bars[i].destroy();
    };
  };

  return {
    setSize: function(name, n) {
      var b = index[name];
      if(b)
        b.setSize(name, n);
    },
    destroy: destroy
  };
};