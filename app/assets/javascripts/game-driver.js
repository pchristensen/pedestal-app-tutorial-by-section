var me = {name: "Me", score: 0};
var players = [me,
               {name: "Fred", score: 0},
               {name: "ahbhgtre", score: 0}];
var game = null;
var gameActive = false;

var rand = function(n){
  return Math.floor(Math.random() * n) + n;
};

var randPlayer = function(){
  return Math.floor(Math.random() * players.length);
};

var sortPlayers = function(){
  players.sort(function(a,b){
    if(a.score < b.score) return 1;
    if(a.score > b.score) return -1;
    return 0;
  });
  for(var i=0;i<players.length;i++){
    players[i].newIndex = i;
  };
};

var updateCounts = function(){
  if(gameActive) {
    var total = 0;
    var max = 0;
    for (var i in players){
      var score = players[i].score;
      total += score;
      if (score > max)
        max = score;
    };
    var avg = total / players.length;

    game.setStat("total-count", total);
    game.setStat("max-count", max);
    game.setStat("average-count", avg);

    setTimeout(updateCounts, 1000);
  }
};

var updateDataflowStats = function(){
  if (gameActive){
    game.setStat("dataflow-time-max", rand(100));
    game.setStat("dataflow-time-avg", rand(50));
    game.setStat("dataflow-time", rand(10));
    setTimeout(updateDataflowStats, 1000);
  }
};

var updatePlayerScores = function(){
  if (gameActive){
    var p = players[randPlayer()];
    if (p.name != "Me"){
      p.score += 1;
      game.setScore(p.name, p.score);
      game.removeBubble();
    };
    updateCounts();
    setTimeout(updatePlayerScores, 1000);
  }
};

var updatePlayerOrder = function(){
  if (gameActive) {
    sortPlayers();
    for (var i in players) {
      var p = players[i];
      game.setOrder(p.name, i);
    };
    setTimeout(updatePlayerOrder, 2000);
  };
};

var startGame = function(){
  console.log("start game");
  game = BubbleGame("game-board");

  gameActive = true;

  game.addHandler(function(points){
    me.score += points;
    game.setScore("Me", me.score);
    updateCounts();
  });

  for (var i in players){
    game.addPlayer(players[i].name);
  };
  updateCounts();
  updateDataflowStats();
  updatePlayerScores();
  updatePlayerOrder();
};

var endGame = function(){
  console.log("end game");
  gameActive = false;
  game.destroy();
};

setTimeout(startGame, 1000);
setTimeout(endGame, 10000);
setTimeout(startGame, 15000);