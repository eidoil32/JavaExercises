package controller;

import magit.Magit;

import java.io.File;

public interface ILoader {
    void execute(String name, File target, Magit magit);
}
