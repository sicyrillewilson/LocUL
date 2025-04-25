import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/batiment.dart';

class BatimentLocal {
  static const _key = 'batiment_cache';

  /// Sauvegarde la liste localement
  static Future<void> save(List<Batiment> batiments) async {
    final prefs = await SharedPreferences.getInstance();
    final jsonStr = jsonEncode(
      batiments.map((b) => b.toJson()).toList(),
    ); // ← sérialise
    await prefs.setString(_key, jsonStr);
  }

  /// Charge la liste ou renvoie null si absente
  static Future<List<Batiment>?> load() async {
    final prefs = await SharedPreferences.getInstance();
    final jsonStr = prefs.getString(_key);
    if (jsonStr == null) return null;
    final List<dynamic> data = jsonDecode(jsonStr);
    return data
        .map((e) => Batiment.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  /// Vide le cache
  static Future<void> clear() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_key);
  }
}
