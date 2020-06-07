package com.github.euonmyoji.bulletcurtain;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author yinyangshi
 */
public class BulletData {
    static final String COMMENT_TYPE = "Comment";
    static final String LIVE_START_TYPE = "LiveStart";
    static final String LIVE_STOP_TYPE = "LiveStop";
    private static final String UNKNOWN_TYPE = "Unknown";
    private final int roomID;
    String type;
    private String userName;
    private String commentText;

    BulletData(JsonElement jsonElement, int roomID, int version) {
        this.roomID = roomID;
        switch (version) {
            case 1: {
                JsonArray obj = jsonElement.getAsJsonArray();
                commentText = obj.get(1).getAsString();
                userName = obj.get(2).getAsJsonArray().get(1).getAsString();
                type = COMMENT_TYPE;
//                RawDataJToken = obj;
                break;
            }
            case 2: {
                JsonObject obj = jsonElement.getAsJsonObject();
//                RawDataJToken = obj;
                String cmd = obj.get("cmd").getAsString();
                switch (cmd) {
                    case "LIVE": {
                        type = LIVE_START_TYPE;
                        break;
                    }
                    case "PREPARING": {
                        type = LIVE_STOP_TYPE;
                        break;
                    }
                    case "DANMU_MSG": {
                        type = COMMENT_TYPE;
                        JsonArray info = obj.getAsJsonArray("info");
                        commentText = info.get(1).getAsString();
//                        UserID = obj["info"][2][0].ToObject < int>();
                        userName = info.get(2).getAsJsonArray().get(1).getAsString();
//                        IsAdmin = obj["info"][2][2].ToString() == "1";
//                        IsVIP = obj["info"][2][3].ToString() == "1";
//                        UserGuardLevel = obj["info"][7].ToObject < int>();
                        break;
                    }
                    case "SEND_GIFT":
//                        Type = BilibiliLiveDanmaku_SocketReceiveDataType.GiftSend;
//                        GiftName = obj["data"]["giftName"].ToString();
//                        userName = obj["data"]["uname"].ToString();
//                        UserID = obj["data"]["uid"].ToObject < int>();
//            //             Giftrcost = obj["data"]["rcost"].ToString();
//                        GiftCount = obj["data"]["num"].ToObject < int>();
                        break;
//                    case "GIFT_TOP": {
//                        Type = BilibiliLiveDanmaku_SocketReceiveDataType.GiftTop;
//                        var alltop = obj["data"].ToList();
//                        GiftRanking = new List<BilibiliLiveDanmaku_GiftRank>();
//                        foreach(var v in alltop)
//                        {
//                            GiftRanking.Add(new BilibiliLiveDanmaku_GiftRank() {
//                                UID =v.Value<int>("uid"),
//                                userName =v.Value<string>("uname"),
//                                Coin =v.Value<decimal>("coin")
//
//                            });
//                        }

//                        break;
//                    }
//                    case "WELCOME": {
//                        Type = BilibiliLiveDanmaku_SocketReceiveDataType.Welcome;
//                        userName = obj["data"]["uname"].ToString();
//                        UserID = obj["data"]["uid"].ToObject < int>();
//                        IsVIP = true;
//                        IsAdmin = obj["data"]["isadmin"] ?.ToString() == "1";
//                        break;
//
//                    }
//                    case "WELCOME_GUARD": {
//                        Type = BilibiliLiveDanmaku_SocketReceiveDataType.WelcomeGuard;
//                        userName = obj["data"]["username"].ToString();
//                        UserID = obj["data"]["uid"].ToObject < int>();
//                        UserGuardLevel = obj["data"]["guard_level"].ToObject < int>();
//                        break;
//                    }
//                    case "GUARD_BUY": {
//                        Type = BilibiliLiveDanmaku_SocketReceiveDataType.GuardBuy;
//                        UserID = obj["data"]["uid"].ToObject < int>();
//                        userName = obj["data"]["username"].ToString();
//                        UserGuardLevel = obj["data"]["guard_level"].ToObject < int>();
//                        GiftName = UserGuardLevel == 3 ? "舰长" :
//                                UserGuardLevel == 2 ? "提督" :
//                                        UserGuardLevel == 1 ? "总督" : "";
//                        GiftCount = obj["data"]["num"].ToObject < int>();
//                        break;
//                    }
//                    case "SUPER_CHAT_MESSAGE": {
//                        Type = BilibiliLiveDanmaku_SocketReceiveDataType.SuperChat;
//                        CommentText = obj["data"]["message"] ?.ToString();
//                        UserID = obj["data"]["uid"].ToObject < int>();
//                        userName = obj["data"]["user_info"]["uname"].ToString();
//                        Price = obj["data"]["price"].ToObject < decimal > ();
//                        SCKeepTime = obj["data"]["time"].ToObject < int>();
//                        break;
//                    }
                    default: {
                        if (cmd.startsWith("DANMU_MSG")) // "高考"fix
                        {
                            type = COMMENT_TYPE;
                            JsonArray info = obj.getAsJsonArray("info");
                            commentText = info.get(1).getAsString();
//                            UserID = obj["info"][2][0].ToObject < int>();
                            userName = info.get(2).getAsJsonArray().get(1).getAsString();
//                            IsAdmin = obj["info"][2][2].ToString() == "1";
//                            IsVIP = obj["info"][2][3].ToString() == "1";
//                            UserGuardLevel = obj["info"][7].ToObject < int>();
                            break;
                        } else {
                            type = UNKNOWN_TYPE;
                        }
                        break;
                    }
                }
                break;
            }
            default: {
                throw new IllegalArgumentException("Version is unknown:" + version);
            }
        }
    }

    @Override
    public String toString() {
        return "[Bilibili Live Room:" + roomID + "]" + userName + ": " + commentText;
    }
}
