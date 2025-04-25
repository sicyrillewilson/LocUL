import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/infrastructure.dart';

class InfrastructureLocal {
  static const _key = 'infrastructure_cache';

  /// Sauvegarde la liste localement
  static Future<void> save(List<Infrastructure> infrastructures) async {
    final prefs = await SharedPreferences.getInstance();
    final jsonStr = jsonEncode(
      infrastructures.map((i) => i.toJson()).toList(),
    ); // ← sérialise
    await prefs.setString(_key, jsonStr);
  }

  /// Charge la liste ou renvoie null si absente
  static Future<List<Infrastructure>?> load() async {
    final prefs = await SharedPreferences.getInstance();
    final jsonStr = prefs.getString(_key);
    if (jsonStr == null) return null;
    final List<dynamic> data = jsonDecode(jsonStr);
    return data
        .map((e) => Infrastructure.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  /// Vide le cache
  static Future<void> clear() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_key);
  }
}
