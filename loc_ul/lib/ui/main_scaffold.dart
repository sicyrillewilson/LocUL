import 'package:flutter/material.dart';
import 'pages/home_page.dart';
import 'pages/batiments_page.dart';
import 'pages/infrastructures_page.dart';
import 'pages/map_page.dart';

class MainScaffold extends StatefulWidget {
  const MainScaffold({super.key});

  @override
  State<MainScaffold> createState() => _MainScaffoldState();
}

class _MainScaffoldState extends State<MainScaffold> {
  int _index = 0;

  final _pages = const [
    HomePage(),
    BatimentsPage(),
    InfrastructuresPage(),
    MapPage(),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: _pages[_index],
      bottomNavigationBar: NavigationBar(
        selectedIndex: _index,
        onDestinationSelected: (i) => setState(() => _index = i),
        labelBehavior: NavigationDestinationLabelBehavior.alwaysShow,
        destinations: const [
          NavigationDestination(icon: Icon(Icons.home), label: 'Accueil'),
          NavigationDestination(
            icon: Icon(Icons.apartment),
            label: 'Bâtiments',
          ),
          NavigationDestination(
            icon: Icon(Icons.factory),
            label: 'Infrastructures',
          ),
          NavigationDestination(icon: Icon(Icons.map), label: 'Maps'),
        ],
      ),
    );
  }
}
