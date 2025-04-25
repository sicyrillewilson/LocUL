import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/salle.dart';

class SalleLocal {
  static const _key = 'salle_cache';

  /// Sauvegarde la liste localement
  static Future<void> save(List<Salle> salles) async {
    final prefs = await SharedPreferences.getInstance();
    final jsonStr = jsonEncode(
      salles.map((s) => s.toJson()).toList(),
    ); // ← sérialise
    await prefs.setString(_key, jsonStr);
  }

  /// Charge la liste ou renvoie null si absente
  static Future<List<Salle>?> load() async {
    final prefs = await SharedPreferences.getInstance();
    final jsonStr = prefs.getString(_key);
    if (jsonStr == null) return null;
    final List<dynamic> data = jsonDecode(jsonStr);
    return data.map((e) => Salle.fromJson(e as Map<String, dynamic>)).toList();
  }

  /// Vide le cache
  static Future<void> clear() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_key);
  }
}
