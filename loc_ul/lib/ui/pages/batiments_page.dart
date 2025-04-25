import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../viewmodels/batiment_vm.dart';
import '../../data/models/batiment.dart';

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
              : Image.network(b.image, width: 56, fit: BoxFit.cover),
      title: Text(b.nom),
      subtitle: Text(b.situation),
      onTap: () {
        // navigation vers une page détail si besoin
        print('Tap sur ${b}');
      },
    );
  }
}
