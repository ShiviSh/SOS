Parse.Cloud.define("sendPushToUser", function(request, response) {
  var senderUser = request.user;
  var appName = request.params.appName;
  var message = request.params.message;

  // Validate that the sender is allowed to send to the recipient.
  // For example each user has an array of objectIds of friends
  /*if (senderUser.get("friendIds").indexOf(recipientUserId) === -1) {
    response.error("The recipient is not the sender's friend, cannot send push.");
  }*/

  // Validate the message text.
  // For example make sure it is under 140 characters
  if (message.length > 140) {
  // Truncate and add a ...
    message = message.substring(0, 137) + "...";
  }

  // Send the push.
  // Find devices associated with the recipient user

  var pushQuery = new Parse.Query(Parse.Installation);
  pushQuery.equalTo("userid",test);
 
  // Send the push notification to results of the query
  Parse.Push.send({
    where: pushQuery,
    data: {
      alert: message
    }
  },{ useMasterKey: true }).then(function() {
      response.success("Push was sent successfully.");
  }, function(error) {
      response.error("Push failed to send with error: " + error.message);
  });
});

var test;
function push(id){
	
	var query = Parse.Query(Parse.Installation);
	query.equalTo("userid",id);
	
	var message = "Someone near you is in trouble";
	
	Parse.Push.send({
    where: query,
    data: {
      alert: message
    }
  },{ success: function () {
                    // Push was successful
                    console.log("Message was sent successfully");
                    
                },
                error: function (error) {
                    
                }, useMasterKey: true});
	
}

var victimid;
var victimlocation;

Parse.Cloud.define("updateUser", function(request, response) 
{
  Parse.Cloud.useMasterKey();
  var query = new Parse.Query(Parse.User);
  var locationquery = new Parse.Query("Request");
  
  victimid = request.params.victimid;
  victimlocation = request.params.victimlocation;
  
  locationquery.withinKilometers("location", request.params.victimlocation, 3);
  locationquery.notEqualTo("userid",request.params.victimid);

  //query.equalTo("objectId", objectId);
  locationquery.find({
      success: function(objects) 
      {
        if(objects.length > 0){
			
			for(var i = 0; i < objects.length; ++i){
				
				query.equalTo("objectId",objects[i].get("userid"));
				query.find({
				success: function(results){
				response.success(results);
				
				if(results.length > 0){
							for(var j = 0; j < results.length; ++j){
								
								
									if(!results[j].get("nearby")){
										
										response.success("nearby");
										Parse.Cloud.useMasterKey();
										
										//results[j].set("nearby",true);
										results[j].save({ "victimid": victimid });
										results[j].save({ "victimlocation" : victimlocation });
										results[j].save({ "nearby": true });
										
										var su = request.user;
										test = results[j].id;
										response.success(results[j].id);
										var pushQuery = new Parse.Query(Parse.Installation);
										 pushQuery.equalTo("userid",results[j].id);
										 pushQuery.find({
											 success: function(res){
												 response.success(res);
											 },
											 error: function(error){
												 response.error(error);
												 
											 }
										 });
										 
										 var msg = "Someone near you is in trouble";
										  // Send the push notification to results of the query
										  Parse.Push.send({
											where: pushQuery,
											data: {
											  alert: msg
											}
										  },{ useMasterKey: true }).then(function() {
											  response.success("Push kkkkkkkkk sent successfully.");
										  }, function(error) {
											  response.error("Push failed to send with error: " + error.message);
										  });
										
										results[j].save();
										
										
									}
								
							}
						}
				
				},
				error: function(error) {
				//error
				console.log("error: " + error);
				}
				});
				//response.success(query.find());
				//response.success("Yes");
			}
		}
        //response.success(objects);
		//response.success("Yes!!!!");
      },
    error: function(error) {
      alert("Error: " + error.code + " " + error.message);
      response.error("Error");
    }
  });
}); 

function user(results){
	
	
	if(results.length > 0){
							for(var j = 0; j < results.length; ++j){
								
								if(results[j].get("nearby") !== null){
									if(!results[j].get("nearby")){
										
										results[j].set("nearby",true);
										push(results[j].get("objectId"));
										results[j].set("victimid",victimid);
										results[j].set("victimlocation",victimlocation);
										results[j].save();
										
										
									}
								}
							}
						}

}
