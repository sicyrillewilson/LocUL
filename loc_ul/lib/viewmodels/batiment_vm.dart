import 'package:flutter/material.dart';
import '../data/models/batiment.dart';
import '../data/repositories/batiment_repo.dart';

class BatimentVM extends ChangeNotifier {
  final BatimentRepo _repo;
  BatimentVM(this._repo);

  List<Batiment>? _items;
  List<Batiment>? get items => _items;
  bool _loading = false;
  bool get loading => _loading;

  Future<void> load() async {
    _loading = true;
    notifyListeners();
    final data = await _repo.fetchBatiments();
    _items = data;
    _loading = false;
    notifyListeners();
  }
}
