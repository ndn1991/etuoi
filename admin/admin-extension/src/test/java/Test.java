import java.util.UUID;

import com.nhb.common.utils.Converter;

public class Test {
	public static void main(String[] args) {
		String[] arr = new String[] { "create_role_a_assign_permision", "create_edit_admin_and_assign_role",
				"create_edit_admin_and_assign_role", "create_role_a_assign_permision", "create_role_a_assign_permision",
				"create_edit_admin_and_assign_role", "per_fetch_permision_a_role", "create_edit_admin_and_assign_role",
				"per_fetch_permision_a_role", "per_fetch_permision_a_role", "change_admin_pass", "userinfo",
				"change_user_asset", "hermes", "fetch_ams_log", "user_detail_log", "change_user_asset",
				"user_detail_log", "inbox", "log_cashin", "ccu", "create_giftcode", "check_giftcode", "merchant",
				"dashboard", "statistic", "cash_out", "update_user", "user_detail_log", "jackpot_log",
				"statistic_by_game_and_bet", "acs", "user_detail_log", "game_running_or_not", "on_off_game",
				"cheat_game", "change_han_muc_am", "check_jp_gb", "give_award", "update_award_config",
				"fetch_remain_awards" };
		long now = System.currentTimeMillis();
		for (String s : arr) {
			String out = "insert ignore into permission(id, name, created_time) values(0x";
			out += Converter.bytesToHex(Converter.uuidToBytes(UUID.randomUUID()));
			out += ", '" + s + "', ";
			out += now;
			out += ");";
			System.out.println(out);
		}
	}
}
