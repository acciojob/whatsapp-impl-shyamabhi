package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
     HashMap<Group, List<User>> groupUserMap;
     HashMap<Group, List<Message>> groupMessageMap;
     HashMap<Message, User> senderMap;
     HashMap<Group, User> adminMap;
     HashSet<String> userMobile;
     int customGroupCount;
     int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    public String createUser(String name, String mobile) throws Exception {//1
        if(userMobile.contains(mobile)){
            throw new Exception("User already exists");
        }
        userMobile.add(mobile);
        User u = new User(name,mobile);
        return "SUCCESS";
    }

    public Group createGroup(List<User> users) {//2
        if(users.size()==2){
            Group gp = new Group(users.get(1).getName(),2);
            groupUserMap.put(gp,users);
            groupMessageMap.put(gp,new ArrayList<>());
            return gp;
        }else{
            customGroupCount++;
            Group gp = new Group("Group "+customGroupCount,users.size());
            groupUserMap.put(gp,users);
            groupMessageMap.put(gp,new ArrayList<>());
            adminMap.put(gp,users.get(0));
            return gp;
        }
    }
    public int createMessage(String content) {//3
        messageId++;
        Message m = new Message(messageId,content);
        return m.getId();
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception{
        if(!groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        if(!groupUserMap.get(group).contains(sender)){
            throw new Exception("You are not allowed to send message");
        }
        groupMessageMap.get(group).add(message);
        senderMap.put(message,sender);
        return groupMessageMap.get(group).size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {
        if(!groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        if(adminMap.get(group)!=approver){
            throw new Exception("Approver does not have rights");
        }
        if(!groupUserMap.get(group).contains(user)){
            throw new Exception("User is not a participant");
        }
        adminMap.replace(group,user);
        return "SUCCESS";
    }

    public int removeUser(User user) throws Exception {
        for(Group g : groupUserMap.keySet()){
            List<User> l = groupUserMap.get(g);
            if(l.contains(user)){
                for(User u : adminMap.values()){
                    if(u == user){
                        throw new Exception("Cannot remove admin\" exception");
                    }
                }
                groupUserMap.get(g).remove(user);

                for(Message m : senderMap.keySet()){
                    User u = senderMap.get(m);
                    if(u == user){
                        senderMap.remove(m);
                        groupMessageMap.get(g).remove(m);
                        return groupUserMap.get(g).size()+groupMessageMap.get(g).size()+senderMap.size();
                    }
                }
            }
        }
        throw new Exception("User not found");
    }

    public String findMessage(Date start, Date end, int k) throws Exception {
        List<Message>l = new ArrayList<>();
        for(Message m : senderMap.keySet()){
            if(m.getTimestamp().compareTo(start)>0 && m.getTimestamp().compareTo(end)<0){
                l.add(m);
            }
        }
        if(l.size()>k){
            throw new Exception("K is greater than the number of messages");
        }else
            return l.get(k).getContent();
    }
}
