package com.magit.tree;

import java.util.ArrayList;
import java.util.List;

public class Tree<T> {
    private Node<T> root;

    public Tree(T rootData) {
        root = new Node<T>(rootData);
    }

    public Node<T> getRoot() {
        return root;
    }

    public void setRoot(Node<T> root) {
        this.root = root;
    }

    public static class Node<T> {
        private T data;
        private Node<T> parent;
        private List<Node<T>> children;

        public Node(T data) {
            this.data = data;
            this.parent = null;
            this.children = new ArrayList<>();
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }

        public Node<T> getParent() {
            return parent;
        }

        public void setParent(Node<T> parent) {
            this.parent = parent;
        }

        public List<Node<T>> getChildren() {
            return children;
        }

        public void addChildren(T file)
        {
            children.add(new Node<T>(file));
        }

        public void addChildren(Node<T> node)
        {
            children.add(node);
        }

        public void setChildren(List<Node<T>> children) {
            this.children = children;
        }
    }
}