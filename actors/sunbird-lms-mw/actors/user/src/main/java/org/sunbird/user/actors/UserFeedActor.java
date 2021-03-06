package org.sunbird.user.actors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.sunbird.actor.core.BaseActor;
import org.sunbird.actor.router.ActorConfig;
import org.sunbird.common.models.response.Response;
import org.sunbird.common.models.util.*;
import org.sunbird.common.request.Request;
import org.sunbird.common.request.RequestContext;
import org.sunbird.dto.SearchDTO;
import org.sunbird.feed.IFeedService;
import org.sunbird.feed.impl.FeedFactory;
import org.sunbird.learner.util.Util;

/** This class contains API related to user feed. */
@ActorConfig(
  tasks = {"getUserFeedById"},
  asyncTasks = {}
)
public class UserFeedActor extends BaseActor {

  @Override
  public void onReceive(Request request) throws Throwable {
    Util.initializeContext(request, TelemetryEnvKey.USER);
    RequestContext context = request.getRequestContext();
    String operation = request.getOperation();
    if (ActorOperations.GET_USER_FEED_BY_ID.getValue().equalsIgnoreCase(operation)) {
      logger.info(context, "UserFeedActor:onReceive getUserFeed method called");
      String userId = (String) request.getRequest().get(JsonKey.USER_ID);
      getUserFeed(userId, context);
    } else {
      onReceiveUnsupportedOperation("UserFeedActor");
    }
  }

  private void getUserFeed(String userId, RequestContext context) {
    IFeedService feedService = FeedFactory.getInstance();
    Map<String, Object> filters = new HashMap<>();
    filters.put(JsonKey.USER_ID, userId);
    SearchDTO search = new SearchDTO();
    search.getAdditionalProperties().put(JsonKey.FILTERS, filters);
    Response userFeedResponse = feedService.search(search, context);
    Map<String, Object> result =
        (Map<String, Object>) userFeedResponse.getResult().get(JsonKey.RESPONSE);
    result.put(
        JsonKey.USER_FEED,
        result.get(JsonKey.CONTENT) == null ? new ArrayList<>() : result.get(JsonKey.CONTENT));
    result.remove(JsonKey.COUNT);
    result.remove(JsonKey.CONTENT);
    sender().tell(userFeedResponse, self());
  }
}
