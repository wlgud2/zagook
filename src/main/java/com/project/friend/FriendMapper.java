package com.project.friend;

import java.util.List;
import java.util.Map;

public interface FriendMapper {

	List<FriendDTO> friendList(Map map);

	int delete_friend(Map map);

	int accept_friend(Map map);

}
