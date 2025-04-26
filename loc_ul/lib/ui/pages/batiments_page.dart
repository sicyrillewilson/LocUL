import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../viewmodels/batiment_vm.dart';
import '../../data/models/batiment.dart';
import 'dart:io';

class BatimentsPage extends StatefulWidget {
  const BatimentsPage({super.key});

  @override
  State<BatimentsPage> createState() => _BatimentsPageState();
}

class _BatimentsPageState extends State<BatimentsPage> {
  @override
  void initState() {
    super.initState();
    // déclenche le chargement une seule fois
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<BatimentVM>().load();
    });
  }

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<BatimentVM>();

    return Scaffold(
      appBar: AppBar(title: const Text('Bâtiments')),
      body:
          vm.loading
              ? const Center(child: CircularProgressIndicator())
              : vm.items == null
              ? const Center(child: Text('Aucun bâtiment'))
              : ListView.builder(
                itemCount: vm.items!.length,
                itemBuilder: (_, i) => _BatimentTile(vm.items![i]),
              ),
    );
  }
}

class _BatimentTile extends StatelessWidget {
  final Batiment b;
  const _BatimentTile(this.b);

  @override
  Widget build(BuildContext context) {
    return ListTile(
      leading:
          b.image.isEmpty
              ? const Icon(Icons.apartment)
              : File(b.image).existsSync()
              ? Image.file(File(b.image), width: 56, fit: BoxFit.cover)
              : const Icon(Icons.broken_image),
      title: Text(b.nom),
      subtitle: Text(b.situation),
      onTap: () {
        // navigation vers une page détail si besoin
        print('Tap sur $b');
        print(
          'Chemin image: ${b.image}  Existe: ${File(b.image).existsSync()}',
        );
      },
    );
  }
}
